-- TituloDocente: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_titulo_docente() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.titulo) = '' THEN
            v_mensaje := fn_msg_vacio('El título');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_titulo_docente_validar ON titulo_docente ^^^
CREATE TRIGGER trg_titulo_docente_validar BEFORE INSERT OR UPDATE ON titulo_docente
    FOR EACH ROW EXECUTE FUNCTION fn_validar_titulo_docente() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_titulo_docente_pertenencia ON titulo_docente ^^^
CREATE TRIGGER trg_titulo_docente_pertenencia BEFORE UPDATE ON titulo_docente
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_docente') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_titulo_docente_sin_borrado ON titulo_docente ^^^
CREATE TRIGGER trg_titulo_docente_sin_borrado BEFORE DELETE ON titulo_docente
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
