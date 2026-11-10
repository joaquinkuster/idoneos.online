-- RespuestaForo: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_respuesta_foro_consulta_activo ON respuesta_foro (id_consulta) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_respuesta_foro() RETURNS trigger AS $$
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

DROP TRIGGER IF EXISTS trg_respuesta_foro_validar ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_validar BEFORE INSERT OR UPDATE ON respuesta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_validar_respuesta_foro() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_respuesta_foro_inmutable ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_inmutable BEFORE UPDATE ON respuesta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_consulta', 'id_docente_curso') ^^^

DROP TRIGGER IF EXISTS trg_respuesta_foro_consulta_activa ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_consulta_activa BEFORE INSERT OR UPDATE OF id_consulta, baja ON respuesta_foro
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_consulta', 'consulta_foro', 'id_consulta') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_respuesta_foro_sin_borrado ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_sin_borrado BEFORE DELETE ON respuesta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
