-- ProgramaUtil Lo que le falta a un programa para estar completo y control de su cronograma.

-- fn_tema_celdas_faltantes(): lo que le falta al contenido de un tema segun lo que prometen las modalidades del curso (CursoService.buscarContenidoDeTema): el video con clon generado, la clase grabada, el material y la autoevaluacion.
CREATE OR REPLACE FUNCTION fn_tema_celdas_faltantes(p_id_tema int, p_id_curso int) RETURNS text[] AS $$
DECLARE
    v_faltan text[] := ARRAY[]::text[];
BEGIN
    IF fn_curso_incluye_modalidad(p_id_curso, 'Con clon de IA')
       AND NOT EXISTS (SELECT 1 FROM tema_recurso tr
                       JOIN recurso r ON r.id_recurso = tr.id_recurso
                       JOIN video_clon vc ON vc.id_recurso = r.id_recurso
                       JOIN estado_generacion eg ON eg.id_estado_generacion = vc.id_estado_generacion
                       WHERE tr.id_tema = p_id_tema AND tr.baja = false AND tr.oculto = false AND r.baja = false
                         AND eg.nombre = 'Generado') THEN
        v_faltan := v_faltan || 'Video clon'::text;
    END IF;
    IF fn_curso_incluye_modalidad(p_id_curso, 'Grabada')
       AND NOT EXISTS (SELECT 1 FROM tema_recurso tr JOIN recurso r ON r.id_recurso = tr.id_recurso
                       WHERE tr.id_tema = p_id_tema AND tr.baja = false AND tr.oculto = false AND r.baja = false
                         AND fn_recurso_modalidad(tr.id_recurso) = 'Grabada') THEN
        v_faltan := v_faltan || 'Clase grabada'::text;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM tema_recurso tr JOIN recurso r ON r.id_recurso = tr.id_recurso
                   WHERE tr.id_tema = p_id_tema AND tr.baja = false AND tr.oculto = false AND r.baja = false
                     AND fn_recurso_modalidad(tr.id_recurso) IS NULL) THEN
        v_faltan := v_faltan || 'Material'::text;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM autoevaluacion a WHERE a.id_tema = p_id_tema AND a.baja = false AND a.oculto = false) THEN
        v_faltan := v_faltan || 'Autoevaluación'::text;
    END IF;
    RETURN v_faltan;
END
$$ LANGUAGE plpgsql STABLE ^^^

-- fn_programa_faltantes(): lo que le falta a un programa para estar completo.
CREATE OR REPLACE FUNCTION fn_programa_faltantes(p_id_programa int) RETURNS text[] AS $$
DECLARE
    v_curso curso%ROWTYPE;
    v_faltantes text[] := ARRAY[]::text[];
    v_datos text[] := ARRAY[]::text[];
    v_dato text;
    v_minimo int := fn_parametro_entero('programa.minimo_temas_con_material', 1);
    v_con_material int;
    v_sin_autoevaluacion text[];
    v_dias int;
    v_temas int;
    v_completa boolean;
    v_tema record;
    v_celdas text[];
BEGIN
    SELECT * INTO v_curso FROM curso WHERE id_curso = (SELECT id_curso FROM programa WHERE id_programa = p_id_programa);
    IF v_curso.id_nivel IS NULL THEN v_datos := v_datos || 'nivel'::text; END IF;
    IF v_curso.objetivos IS NULL OR btrim(v_curso.objetivos) = '' THEN v_datos := v_datos || 'objetivos'::text; END IF;
    IF v_curso.emite_certificado AND (v_curso.carga_horaria_minutos IS NULL OR v_curso.carga_horaria_minutos <= 0) THEN
        v_datos := v_datos || 'carga horaria'::text;
    END IF;
    SELECT coalesce(sum(duracion_dias), 0), count(*) INTO v_dias, v_temas
        FROM tema_cronograma WHERE id_programa = p_id_programa AND baja = false;
    v_completa := v_temas > 0 AND v_dias = v_curso.duracion_dias;

    IF cardinality(v_datos) > 0 THEN
        v_faltantes := v_faltantes || ('Faltan completar los datos del curso: ' || array_to_string(v_datos, ', '));
    END IF;
    SELECT count(*) INTO v_con_material FROM tema_cronograma tc
        WHERE tc.id_programa = p_id_programa AND tc.baja = false
          AND EXISTS (SELECT 1 FROM autoevaluacion a WHERE a.id_tema = tc.id_tema AND a.baja = false AND a.oculto = false)
          AND EXISTS (SELECT 1 FROM tema_recurso tr WHERE tr.id_tema = tc.id_tema AND tr.baja = false AND tr.oculto = false);
    IF v_con_material < v_minimo THEN
        v_faltantes := v_faltantes || ('Faltan temas con al menos un material y una autoevaluación publicados: tiene ' || v_con_material
                || ' de ' || v_minimo || ' requeridos');
    END IF;
    SELECT array_agg('«' || t.titulo || '»' ORDER BY tc.numero_orden) INTO v_sin_autoevaluacion
        FROM tema_cronograma tc JOIN tema t ON t.id_tema = tc.id_tema
        WHERE tc.id_programa = p_id_programa AND tc.baja = false
          AND NOT EXISTS (SELECT 1 FROM autoevaluacion a WHERE a.id_tema = tc.id_tema AND a.baja = false AND a.oculto = false);
    IF v_sin_autoevaluacion IS NOT NULL THEN
        v_faltantes := v_faltantes || ('Faltan autoevaluaciones visibles en: ' || array_to_string(v_sin_autoevaluacion, ', '));
    END IF;
    IF NOT v_completa THEN
        v_faltantes := v_faltantes || ('La duración asignada a los temas (' || fn_msg_duracion(v_dias) || ') no coincide con la del curso ('
                || fn_msg_duracion(v_curso.duracion_dias) || ')');
    END IF;
    FOREACH v_dato IN ARRAY v_datos LOOP
        v_faltantes := v_faltantes || ('Al curso le falta: ' || v_dato);
    END LOOP;
    IF NOT v_completa THEN
        v_faltantes := v_faltantes || 'La duración del curso no está asignada por completo a los temas del cronograma'::text;
    END IF;
    FOR v_tema IN SELECT tc.id_tema, t.titulo FROM tema_cronograma tc JOIN tema t ON t.id_tema = tc.id_tema
                  WHERE tc.id_programa = p_id_programa AND tc.baja = false ORDER BY tc.numero_orden LOOP
        v_celdas := fn_tema_celdas_faltantes(v_tema.id_tema, v_curso.id_curso);
        IF cardinality(v_celdas) > 0 THEN
            v_faltantes := v_faltantes || ('El tema «' || v_tema.titulo || '» no tiene: ' || array_to_string(v_celdas, ', '));
        END IF;
    END LOOP;
    RETURN v_faltantes;
END
$$ LANGUAGE plpgsql STABLE ^^^

-- Cronograma completo (TemaCronogramaService): los dias de los temas suman siempre la duracion del curso.

-- fn_cronograma_completo(): se ejecuta al terminar la operacion cada vez que cambia el cronograma de un programa, el programa o la
-- duracion del curso, y verifica el cronograma de cada programa activo afectado.
CREATE OR REPLACE FUNCTION fn_cronograma_completo() RETURNS trigger AS $$
DECLARE
    v_programa record;
    v_minimo int := fn_parametro_entero('programa.minimo_temas_con_material', 1);
    v_dias int;
    v_temas int;
    v_id_programa int;
    v_id_curso int;
BEGIN
    IF TG_TABLE_NAME = 'tema_cronograma' THEN
        IF TG_OP = 'DELETE' THEN v_id_programa := OLD.id_programa; ELSE v_id_programa := NEW.id_programa; END IF;
    ELSIF TG_TABLE_NAME = 'programa' THEN
        v_id_programa := NEW.id_programa;
    ELSE
        v_id_curso := NEW.id_curso;
    END IF;
    FOR v_programa IN SELECT p.id_programa, p.nombre, c.duracion_dias FROM programa p JOIN curso c ON c.id_curso = p.id_curso
                      WHERE p.baja = false AND (p.id_programa = v_id_programa OR p.id_curso = v_id_curso) LOOP
        SELECT coalesce(sum(duracion_dias), 0), count(*) INTO v_dias, v_temas
            FROM tema_cronograma WHERE id_programa = v_programa.id_programa AND baja = false;
        IF v_temas < v_minimo THEN
            RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el cronograma del programa', v_programa.nombre,
                    'tiene ' || fn_msg_cantidad(v_temas, 'tema', 'temas') || ' y hacen falta al menos ' || v_minimo,
                    'Sumá temas con el contenido completo hasta llegar al mínimo.')
                USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
        END IF;
        IF v_dias <> v_programa.duracion_dias THEN
            RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el cronograma del programa', v_programa.nombre,
                    'asigna ' || fn_msg_duracion(v_dias) || ' y el curso dura ' || fn_msg_duracion(v_programa.duracion_dias),
                    'Asigná entre los temas los ' || fn_msg_duracion(v_programa.duracion_dias) || ' del curso.')
                USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
        END IF;
    END LOOP;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^
