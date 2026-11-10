-- DetalleAuditoria: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_detalle_auditoria_historial ON detalle_auditoria ^^^
CREATE TRIGGER trg_detalle_auditoria_historial BEFORE UPDATE ON detalle_auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_historial() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un historial: lo que se registró no se modifica.
DROP TRIGGER IF EXISTS trg_detalle_auditoria_historial ON detalle_auditoria ^^^
CREATE TRIGGER trg_detalle_auditoria_historial BEFORE UPDATE ON detalle_auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_historial() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_detalle_auditoria_sin_borrado ON detalle_auditoria ^^^
CREATE TRIGGER trg_detalle_auditoria_sin_borrado BEFORE DELETE ON detalle_auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
