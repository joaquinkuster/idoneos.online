-- Comprobante: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_comprobante() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.dni_pagador !~ '^[0-9]{7,8}$' THEN
            v_mensaje := fn_msg_formato('El DNI', 'solo números, 7 u 8 dígitos');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_comprobante_validar ON comprobante ^^^
CREATE TRIGGER trg_comprobante_validar BEFORE INSERT OR UPDATE ON comprobante
    FOR EACH ROW EXECUTE FUNCTION fn_validar_comprobante() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

-- Un comprobante es el respaldo de un pago aprobado: solo se actualiza si se envio
DROP TRIGGER IF EXISTS trg_comprobante_inmutable ON comprobante ^^^
CREATE TRIGGER trg_comprobante_inmutable BEFORE UPDATE ON comprobante
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('numero', 'id_pago', 'dni_pagador', 'nombre_pagador', 'fecha_emision') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_comprobante_sin_borrado ON comprobante ^^^
CREATE TRIGGER trg_comprobante_sin_borrado BEFORE DELETE ON comprobante
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
