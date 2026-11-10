-- Certificado: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_certificado() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.dni_alumno !~ '^[0-9]{7,8}$' THEN
            v_mensaje := fn_msg_formato('El DNI', 'solo números, 7 u 8 dígitos');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_certificado_validar ON certificado ^^^
CREATE TRIGGER trg_certificado_validar BEFORE INSERT OR UPDATE ON certificado
    FOR EACH ROW EXECUTE FUNCTION fn_validar_certificado() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_certificado_inmutable ON certificado ^^^
CREATE TRIGGER trg_certificado_inmutable BEFORE UPDATE ON certificado
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('numero', 'id_inscripcion', 'fecha_emision') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_certificado_sin_borrado ON certificado ^^^
CREATE TRIGGER trg_certificado_sin_borrado BEFORE DELETE ON certificado
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
