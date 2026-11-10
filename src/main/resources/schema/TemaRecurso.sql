-- TemaRecurso: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_tema_recurso_recurso_activo ON tema_recurso (id_recurso) WHERE baja = false ^^^

CREATE INDEX IF NOT EXISTS ix_tema_recurso_tema_activo ON tema_recurso (id_tema) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_tema_recurso_tema_activa ON tema_recurso ^^^
CREATE TRIGGER trg_tema_recurso_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON tema_recurso
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- Ocultar o dar de baja un recurso no puede dejar incompleto el diseño del curso (RecursoService, ProgramaService).

-- fn_tema_recurso_despublicar(): disparador de un recurso publicado que se oculta o se da de baja en su tema.
CREATE OR REPLACE FUNCTION fn_tema_recurso_despublicar() RETURNS trigger AS $$
DECLARE
    v_titulo text;
    v_tema text;
    v_curso int;
    v_minimo int := fn_parametro_entero('programa.minimo_temas_con_material', 1);
    v_programa record;
    v_antes int;
    v_motivo text;
    v_celda text;
    v_aplica boolean;
    v_tiene_antes boolean;
    v_tiene_despues boolean;
BEGIN
    SELECT titulo INTO v_titulo FROM recurso WHERE id_recurso = OLD.id_recurso;
    IF EXISTS (SELECT 1 FROM recurso WHERE id_recurso = OLD.id_recurso AND baja = true) THEN
        RETURN NEW;
    END IF;
    SELECT titulo, id_curso INTO v_tema, v_curso FROM tema WHERE id_tema = OLD.id_tema;

    -- 1) El programa conserva el minimo de temas con material publicado y autoevaluacion
    IF EXISTS (SELECT 1 FROM autoevaluacion a WHERE a.id_tema = OLD.id_tema AND a.baja = false AND a.oculto = false)
       AND NOT EXISTS (SELECT 1 FROM tema_recurso tr WHERE tr.id_tema = OLD.id_tema AND tr.baja = false AND tr.oculto = false
                         AND tr.id_tema_recurso <> OLD.id_tema_recurso) THEN
        FOR v_programa IN SELECT DISTINCT tc.id_programa FROM tema_cronograma tc JOIN programa p ON p.id_programa = tc.id_programa
                          WHERE tc.id_tema = OLD.id_tema AND tc.baja = false AND p.baja = false ORDER BY tc.id_programa LOOP
            SELECT count(*) INTO v_antes FROM tema_cronograma tc
                WHERE tc.id_programa = v_programa.id_programa AND tc.baja = false
                  AND EXISTS (SELECT 1 FROM autoevaluacion a WHERE a.id_tema = tc.id_tema AND a.baja = false AND a.oculto = false)
                  AND EXISTS (SELECT 1 FROM tema_recurso tr WHERE tr.id_tema = tc.id_tema AND tr.baja = false AND tr.oculto = false);
            IF v_antes - 1 < v_minimo THEN
                v_motivo := fn_msg_diseno_incompleto('es el único material publicado del tema «' || v_tema || '»');
                IF NEW.baja = true AND OLD.baja = false THEN
                    RAISE EXCEPTION '%', fn_msg_baja('el recurso', v_titulo, v_motivo, 'Registrá otro antes de darlo de baja.')
                        USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
                END IF;
                RAISE EXCEPTION '%', fn_msg_accion('ocultar', 'el recurso', v_titulo, v_motivo, 'Registrá otro antes de ocultarlo.')
                    USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
            END IF;
        END LOOP;
    END IF;

    -- 2) El tema no pierde su unico video con clon generado, su unica clase grabada ni su unico material
    IF EXISTS (SELECT 1 FROM tema_cronograma tc JOIN programa p ON p.id_programa = tc.id_programa
               WHERE tc.id_tema = OLD.id_tema AND tc.baja = false AND p.baja = false) THEN
        FOREACH v_celda IN ARRAY ARRAY['clon', 'grabada', 'material'] LOOP
            v_aplica := CASE v_celda WHEN 'clon' THEN fn_curso_incluye_modalidad(v_curso, 'Con clon de IA')
                                     WHEN 'grabada' THEN fn_curso_incluye_modalidad(v_curso, 'Grabada') ELSE true END;
            SELECT coalesce(bool_or(CASE v_celda
                        WHEN 'clon' THEN EXISTS (SELECT 1 FROM video_clon vc JOIN estado_generacion eg ON eg.id_estado_generacion = vc.id_estado_generacion
                                                 WHERE vc.id_recurso = tr.id_recurso AND eg.nombre = 'Generado')
                        WHEN 'grabada' THEN fn_recurso_modalidad(tr.id_recurso) = 'Grabada'
                        ELSE fn_recurso_modalidad(tr.id_recurso) IS NULL END), false)
                INTO v_tiene_antes
                FROM tema_recurso tr JOIN recurso r ON r.id_recurso = tr.id_recurso
                WHERE tr.id_tema = OLD.id_tema AND tr.baja = false AND tr.oculto = false AND r.baja = false;
            SELECT coalesce(bool_or(CASE v_celda
                        WHEN 'clon' THEN EXISTS (SELECT 1 FROM video_clon vc JOIN estado_generacion eg ON eg.id_estado_generacion = vc.id_estado_generacion
                                                 WHERE vc.id_recurso = tr.id_recurso AND eg.nombre = 'Generado')
                        WHEN 'grabada' THEN fn_recurso_modalidad(tr.id_recurso) = 'Grabada'
                        ELSE fn_recurso_modalidad(tr.id_recurso) IS NULL END), false)
                INTO v_tiene_despues
                FROM tema_recurso tr JOIN recurso r ON r.id_recurso = tr.id_recurso
                WHERE tr.id_tema = OLD.id_tema AND tr.baja = false AND tr.oculto = false AND r.baja = false
                  AND tr.id_tema_recurso <> OLD.id_tema_recurso;
            IF v_aplica AND v_tiene_antes AND NOT v_tiene_despues THEN
                v_motivo := fn_msg_diseno_incompleto('es ' || CASE v_celda WHEN 'clon' THEN 'el único video clon generado'
                                                                         WHEN 'grabada' THEN 'la única clase grabada publicada'
                                                                         ELSE 'el único material publicado' END
                                                     || ' del tema «' || v_tema || '»');
                IF NEW.baja = true AND OLD.baja = false THEN
                    RAISE EXCEPTION '%', fn_msg_baja('el recurso', v_titulo, v_motivo, 'Registrá otro antes de darlo de baja.')
                        USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
                END IF;
                RAISE EXCEPTION '%', fn_msg_accion('ocultar', 'el recurso', v_titulo, v_motivo, 'Registrá otro antes de ocultarlo.')
                    USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
            END IF;
        END LOOP;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_recurso_despublicar ON tema_recurso ^^^
CREATE TRIGGER trg_tema_recurso_despublicar BEFORE UPDATE OF oculto, baja ON tema_recurso
    FOR EACH ROW WHEN (OLD.baja = false AND OLD.oculto = false AND (NEW.baja = true OR NEW.oculto = true))
    EXECUTE FUNCTION fn_tema_recurso_despublicar() ^^^

-- fn_tema_recurso_registro(): disparador del alta de un recurso en un tema. Un recurso no se registra dos veces en el mismo tema y una
-- grabacion solo se registra en un curso con la modalidad Grabada.
CREATE OR REPLACE FUNCTION fn_tema_recurso_registro() RETURNS trigger AS $$
DECLARE
    v_titulo text;
    v_tema text;
    v_curso int;
BEGIN
    IF NEW.baja = true THEN
        RETURN NEW;
    END IF;
    SELECT titulo INTO v_titulo FROM recurso WHERE id_recurso = NEW.id_recurso;
    SELECT titulo, id_curso INTO v_tema, v_curso FROM tema WHERE id_tema = NEW.id_tema;
    IF EXISTS (SELECT 1 FROM tema_recurso WHERE id_tema = NEW.id_tema AND id_recurso = NEW.id_recurso AND baja = false
                 AND id_tema_recurso IS DISTINCT FROM NEW.id_tema_recurso) THEN
        RAISE EXCEPTION '%', fn_msg_accion('registrar', 'el recurso', v_titulo, 'ya está en el tema «' || v_tema || '»', 'Elegí otro tema.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF EXISTS (SELECT 1 FROM grabacion WHERE id_recurso = NEW.id_recurso) AND NOT fn_curso_incluye_modalidad(v_curso, 'Grabada') THEN
        RAISE EXCEPTION 'Error! El curso no tiene la modalidad ''Grabada'': no puede registrarse una grabación.'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_recurso_registro ON tema_recurso ^^^
CREATE TRIGGER trg_tema_recurso_registro BEFORE INSERT ON tema_recurso
    FOR EACH ROW EXECUTE FUNCTION fn_tema_recurso_registro() ^^^

-- fn_tema_recurso_publicar(): disparador de la publicacion de un recurso oculto. No se publica uno cuya modalidad el curso ya no tiene.
CREATE OR REPLACE FUNCTION fn_tema_recurso_publicar() RETURNS trigger AS $$
DECLARE
    v_modalidad text := fn_recurso_modalidad(OLD.id_recurso);
    v_titulo text;
    v_curso int;
BEGIN
    SELECT id_curso INTO v_curso FROM tema WHERE id_tema = OLD.id_tema;
    IF v_modalidad IS NOT NULL AND NOT fn_curso_incluye_modalidad(v_curso, v_modalidad) THEN
        SELECT titulo INTO v_titulo FROM recurso WHERE id_recurso = OLD.id_recurso;
        RAISE EXCEPTION 'Error! No se puede publicar el recurso «%»: el curso no tiene la modalidad ''%''.', v_titulo, v_modalidad
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_recurso_publicar ON tema_recurso ^^^
CREATE TRIGGER trg_tema_recurso_publicar BEFORE UPDATE OF oculto ON tema_recurso
    FOR EACH ROW WHEN (OLD.oculto = true AND NEW.oculto = false AND NEW.baja = false)
    EXECUTE FUNCTION fn_tema_recurso_publicar() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_tema_recurso_pertenencia ON tema_recurso ^^^
CREATE TRIGGER trg_tema_recurso_pertenencia BEFORE UPDATE ON tema_recurso
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema', 'id_recurso', 'id_docente_curso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_tema_recurso_sin_borrado ON tema_recurso ^^^
CREATE TRIGGER trg_tema_recurso_sin_borrado BEFORE DELETE ON tema_recurso
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
