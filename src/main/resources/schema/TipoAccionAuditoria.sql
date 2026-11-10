-- TipoAccionAuditoria: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_tipo_accion_auditoria_predefinido ON tipo_accion_auditoria ^^^
CREATE TRIGGER trg_tipo_accion_auditoria_predefinido BEFORE INSERT OR UPDATE OR DELETE ON tipo_accion_auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_tipo_accion_auditoria_sin_borrado ON tipo_accion_auditoria ^^^
CREATE TRIGGER trg_tipo_accion_auditoria_sin_borrado BEFORE DELETE ON tipo_accion_auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
