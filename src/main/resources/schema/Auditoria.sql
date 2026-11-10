-- Auditoria: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_auditoria_historial ON auditoria ^^^
CREATE TRIGGER trg_auditoria_historial BEFORE UPDATE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_historial() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un historial: lo que se registró no se modifica.
DROP TRIGGER IF EXISTS trg_auditoria_historial ON auditoria ^^^
CREATE TRIGGER trg_auditoria_historial BEFORE UPDATE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_historial() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_auditoria_sin_borrado ON auditoria ^^^
CREATE TRIGGER trg_auditoria_sin_borrado BEFORE DELETE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
