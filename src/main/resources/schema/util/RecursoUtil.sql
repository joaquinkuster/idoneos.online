-- RecursoUtil Modalidad de un recurso y regla de los recursos que algún tema usa.

-- Los datos de un recurso (titulo, descripcion, archivo, autor, identificador) no se modifican mientras algun tema lo usa.

-- fn_recurso_generado_por_ia(p_id_recurso): true si el recurso es un material generado con inteligencia artificial. Mientras está oculto para los
-- alumnos, el docente lo puede editar aunque esté en un tema: lo usa como borrador hasta que lo publica.
CREATE OR REPLACE FUNCTION fn_recurso_generado_por_ia(p_id_recurso int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM documento d JOIN material_didactico m ON m.id_documento = d.id_documento
                   WHERE d.id_recurso = p_id_recurso AND m.id_estado_generacionia IS NOT NULL)
$$ LANGUAGE sql STABLE ^^^

-- fn_recurso_en_uso(): disparador de modificacion de un recurso o de los datos propios de su tipo. Rechaza el cambio si algun tema activo
-- usa el recurso.
CREATE OR REPLACE FUNCTION fn_recurso_en_uso() RETURNS trigger AS $$
DECLARE
    v_usos bigint;
    v_titulo text;
BEGIN
    SELECT count(*) INTO v_usos FROM tema_recurso WHERE id_recurso = OLD.id_recurso AND baja = false
        AND NOT (oculto AND fn_recurso_generado_por_ia(OLD.id_recurso));
    IF v_usos >= 1 THEN
        SELECT titulo INTO v_titulo FROM recurso WHERE id_recurso = OLD.id_recurso;
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el recurso', v_titulo,
                'tiene ' || fn_msg_cantidad(v_usos, 'tema asociado', 'temas asociados'), '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

-- fn_recurso_en_uso_de_subtipo(): igual que fn_recurso_en_uso para los datos propios de un material bibliográfico o didáctico, que cuelgan
-- del documento y no del recurso.
CREATE OR REPLACE FUNCTION fn_recurso_en_uso_de_subtipo() RETURNS trigger AS $$
DECLARE
    v_id_recurso int;
    v_usos bigint;
    v_titulo text;
BEGIN
    SELECT id_recurso INTO v_id_recurso FROM documento WHERE id_documento = OLD.id_documento;
    SELECT count(*) INTO v_usos FROM tema_recurso WHERE id_recurso = v_id_recurso AND baja = false
        AND NOT (oculto AND fn_recurso_generado_por_ia(v_id_recurso));
    IF v_usos >= 1 THEN
        SELECT titulo INTO v_titulo FROM recurso WHERE id_recurso = v_id_recurso;
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el recurso', v_titulo,
                'tiene ' || fn_msg_cantidad(v_usos, 'tema asociado', 'temas asociados'), '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

-- Registrar un recurso en un tema, y publicarlo

-- fn_recurso_modalidad(p_id_recurso): funcion auxiliar.
CREATE OR REPLACE FUNCTION fn_recurso_modalidad(p_id_recurso int) RETURNS text AS $$
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM grabacion g WHERE g.id_recurso = p_id_recurso) THEN
            CASE WHEN EXISTS (SELECT 1 FROM clase_en_vivo c JOIN grabacion g ON g.id_grabacion = c.id_grabacion
                              WHERE g.id_recurso = p_id_recurso AND c.baja = false)
                 THEN 'En vivo' ELSE 'Grabada' END
        WHEN EXISTS (SELECT 1 FROM video_clon v WHERE v.id_recurso = p_id_recurso) THEN 'Con clon de IA'
    END
$$ LANGUAGE sql STABLE ^^^

-- fn_curso_incluye_modalidad(p_id_curso, p_modalidad): funcion auxiliar. Devuelve true si el curso tiene la modalidad.
CREATE OR REPLACE FUNCTION fn_curso_incluye_modalidad(p_id_curso int, p_modalidad text) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM curso_modalidad cm JOIN modalidad m ON m.id_modalidad = cm.id_modalidad
                   WHERE cm.id_curso = p_id_curso AND m.nombre = p_modalidad)
$$ LANGUAGE sql STABLE ^^^

-- fn_documento_en_uso_por_ia(p_id_documento): true si el documento es fuente o evidencia de contenido generado con inteligencia artificial que sigue
-- vigente: un banco de preguntas o un material sin baja y que no falló.
CREATE OR REPLACE FUNCTION fn_documento_en_uso_por_ia(p_id_documento int) RETURNS boolean AS $$
BEGIN
    RETURN EXISTS (
            SELECT 1 FROM pool_fuente pf
            JOIN pool p ON p.id_pool = pf.id_pool
            LEFT JOIN estado_generacionia e ON e.id_estado_generacionia = p.id_estado_generacionia
            WHERE pf.id_documento = p_id_documento AND p.baja = false AND COALESCE(e.nombre, '') <> 'Fallido')
        OR EXISTS (
            SELECT 1 FROM documento_fuente df
            JOIN documento g ON g.id_documento = df.id_documento
            JOIN recurso rg ON rg.id_recurso = g.id_recurso
            LEFT JOIN material_didactico m ON m.id_documento = g.id_documento
            LEFT JOIN estado_generacionia e ON e.id_estado_generacionia = m.id_estado_generacionia
            WHERE df.id_documento_fuente = p_id_documento AND rg.baja = false AND COALESCE(e.nombre, '') <> 'Fallido')
        OR EXISTS (
            SELECT 1 FROM pregunta q
            JOIN pool p ON p.id_pool = q.id_pool
            JOIN oracion o ON o.id_oracion = q.id_oracion_evidencia
            JOIN indice_documento i ON i.id_indice_documento = o.id_indice_documento
            LEFT JOIN estado_generacionia e ON e.id_estado_generacionia = p.id_estado_generacionia
            WHERE i.id_documento = p_id_documento AND q.baja = false AND p.baja = false AND COALESCE(e.nombre, '') <> 'Fallido');
END
$$ LANGUAGE plpgsql STABLE ^^^

-- fn_documento_bloqueado_por_ia(): disparador de modificación (argumento 'modificar') o de baja (argumento 'dar de baja') de un documento o de lo que
-- cuelga de él (su recurso, su subtipo, su índice). Rechaza el cambio si el documento es fuente o evidencia de contenido generado con IA vigente.
CREATE OR REPLACE FUNCTION fn_documento_bloqueado_por_ia() RETURNS trigger AS $$
DECLARE
    v_id_documento int;
    v_titulo text;
BEGIN
    IF TG_TABLE_NAME = 'recurso' THEN
        SELECT id_documento INTO v_id_documento FROM documento WHERE id_recurso = OLD.id_recurso;
    ELSE
        v_id_documento := OLD.id_documento;
    END IF;
    IF v_id_documento IS NOT NULL AND fn_documento_en_uso_por_ia(v_id_documento) THEN
        SELECT r.titulo INTO v_titulo FROM documento d JOIN recurso r ON r.id_recurso = d.id_recurso WHERE d.id_documento = v_id_documento;
        RAISE EXCEPTION '%', 'Error! No se puede ' || TG_ARGV[0] || ' el documento «' || v_titulo
                || '» porque es fuente o evidencia de contenido generado con inteligencia artificial.'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^
