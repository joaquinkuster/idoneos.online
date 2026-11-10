-- Pago: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_pago() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.monto < 0 THEN
            v_mensaje := fn_msg_formato('El monto', 'número desde 0');
        ELSIF NEW.ultimos_digitos_tarjeta IS NOT NULL AND NEW.ultimos_digitos_tarjeta !~ '^[0-9]{1,4}$' THEN
            v_mensaje := fn_msg_formato('Los últimos dígitos de la tarjeta', 'solo números, hasta 4');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pago_validar ON pago ^^^
CREATE TRIGGER trg_pago_validar BEFORE INSERT OR UPDATE ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_validar_pago() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- Pago aprobado: un pago aprobado ya no admite cambios de estado, monto.
CREATE OR REPLACE FUNCTION fn_pago_aprobado() RETURNS trigger AS $$
BEGIN
    IF (SELECT nombre FROM estado_pago WHERE id_estado_pago = OLD.id_estado_pago) = 'Aprobado' THEN
        RAISE EXCEPTION 'No se puede modificar un pago aprobado'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pago_aprobado ON pago ^^^
CREATE TRIGGER trg_pago_aprobado BEFORE UPDATE ON pago
    FOR EACH ROW WHEN (OLD.id_estado_pago IS DISTINCT FROM NEW.id_estado_pago
                    OR OLD.monto IS DISTINCT FROM NEW.monto
                    OR OLD.numero_comprobante IS DISTINCT FROM NEW.numero_comprobante
                    OR OLD.id_inscripcion IS DISTINCT FROM NEW.id_inscripcion)
    EXECUTE FUNCTION fn_pago_aprobado() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_pago_pertenencia ON pago ^^^
CREATE TRIGGER trg_pago_pertenencia BEFORE UPDATE ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_inscripcion') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_pago_sin_borrado ON pago ^^^
CREATE TRIGGER trg_pago_sin_borrado BEFORE DELETE ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
