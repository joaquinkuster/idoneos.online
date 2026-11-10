-- Descuento: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_descuento() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.nombre) = '' THEN
            v_mensaje := 'Error! Faltan completar los campos obligatorios: nombre.';
        ELSIF NEW.porcentaje < 1 OR NEW.porcentaje > 99 THEN
            v_mensaje := fn_msg_formato('El porcentaje', 'número de 1 a 99');
        ELSIF NEW.vigencia_hasta <= NEW.vigencia_desde THEN
            v_mensaje := 'Error! La vigencia hasta debe ser posterior a la vigencia desde.';
        ELSIF NEW.cantidad_limite <= 0 THEN
            v_mensaje := fn_msg_formato('La cantidad límite', 'número entero mayor a cero');
        ELSIF NEW.cursos_requeridos < 0 THEN
            v_mensaje := fn_msg_formato('La cantidad de cursos requeridos', 'número entero mayor o igual a cero');
        ELSIF NEW.cantidad_usada < 0 THEN
            v_mensaje := fn_msg_formato('La cantidad usada', 'número entero desde cero');
        ELSIF NEW.cantidad_usada > NEW.cantidad_limite THEN
            v_mensaje := 'Error! La cantidad límite no puede ser menor a la cantidad ya usada (' || NEW.cantidad_usada || ').';
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_descuento_validar ON descuento ^^^
CREATE TRIGGER trg_descuento_validar BEFORE INSERT OR UPDATE ON descuento
    FOR EACH ROW EXECUTE FUNCTION fn_validar_descuento() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_descuento(): disparador de baja de un descuento. Rechaza la baja si el descuento ya se uso (cantidad_usada
-- mayor que cero).
CREATE OR REPLACE FUNCTION fn_baja_descuento() RETURNS trigger AS $$
BEGIN
    IF OLD.cantidad_usada > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('el descuento', OLD.nombre,
                'se aplicó a ' || fn_msg_cantidad(OLD.cantidad_usada, 'inscripción', 'inscripciones'),
                'Un descuento aplicado ya no puede darse de baja; se desactiva solo al vencer o al agotarse.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_descuento_baja ON descuento ^^^
CREATE TRIGGER trg_descuento_baja BEFORE UPDATE OF baja ON descuento
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_descuento() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_descuento_sin_borrado ON descuento ^^^
CREATE TRIGGER trg_descuento_sin_borrado BEFORE DELETE ON descuento
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
