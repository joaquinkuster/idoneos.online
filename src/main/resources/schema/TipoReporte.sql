-- TipoReporte: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_tipo_reporte_predefinido ON tipo_reporte ^^^
CREATE TRIGGER trg_tipo_reporte_predefinido BEFORE INSERT OR UPDATE OR DELETE ON tipo_reporte
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_tipo_reporte_sin_borrado ON tipo_reporte ^^^
CREATE TRIGGER trg_tipo_reporte_sin_borrado BEFORE DELETE ON tipo_reporte
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
