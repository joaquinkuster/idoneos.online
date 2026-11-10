-- ConsultaForo: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_consulta_foro_tema_activo ON consulta_foro (id_tema) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_consulta_foro() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.texto) = '' THEN
            v_mensaje := fn_msg_vacio('El texto');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_consulta_foro_validar ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_validar BEFORE INSERT OR UPDATE ON consulta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_validar_consulta_foro() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_consulta_foro_inmutable ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_inmutable BEFORE UPDATE ON consulta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema', 'id_inscripcion') ^^^

DROP TRIGGER IF EXISTS trg_consulta_foro_tema_activa ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON consulta_foro
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

-- ----------------------------------------------------------------------
-- Cascadas de baja
-- ----------------------------------------------------------------------

-- fn_cascada_consulta(): al dar de baja una consulta del foro se dan de baja sus respuestas.
CREATE OR REPLACE FUNCTION fn_cascada_consulta() RETURNS trigger AS $$
BEGIN
    UPDATE respuesta_foro SET baja = true, ultima_modificacion = now() WHERE id_consulta = NEW.id_consulta AND baja = false;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_consulta_foro_cascada ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_cascada AFTER UPDATE OF baja ON consulta_foro
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_cascada_consulta() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_consulta_foro_sin_borrado ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_sin_borrado BEFORE DELETE ON consulta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
