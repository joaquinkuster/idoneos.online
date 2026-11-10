-- Docente: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_docente() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.anios_experiencia IS NOT NULL AND NEW.anios_experiencia < 0 THEN
            v_mensaje := fn_msg_formato('Los años de experiencia', 'número entero desde 0');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_docente_validar ON docente ^^^
CREATE TRIGGER trg_docente_validar BEFORE INSERT OR UPDATE ON docente
    FOR EACH ROW EXECUTE FUNCTION fn_validar_docente() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_docente_pertenencia ON docente ^^^
CREATE TRIGGER trg_docente_pertenencia BEFORE UPDATE ON docente
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_usuario') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_docente_sin_borrado ON docente ^^^
CREATE TRIGGER trg_docente_sin_borrado BEFORE DELETE ON docente
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
