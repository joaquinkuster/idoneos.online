-- Usuario: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_usuario() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.dni !~ '^[0-9]{7,8}$' THEN
            v_mensaje := fn_msg_formato('El DNI', 'solo números, 7 u 8 dígitos');
        ELSIF btrim(NEW.nombre) = '' OR btrim(NEW.apellido) = '' THEN
            v_mensaje := fn_msg_vacio('El nombre y el apellido');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_usuario_validar ON usuario ^^^
CREATE TRIGGER trg_usuario_validar BEFORE INSERT OR UPDATE ON usuario
    FOR EACH ROW EXECUTE FUNCTION fn_validar_usuario() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_usuario_sin_borrado ON usuario ^^^
CREATE TRIGGER trg_usuario_sin_borrado BEFORE DELETE ON usuario
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
