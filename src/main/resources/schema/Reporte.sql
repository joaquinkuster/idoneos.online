-- Reporte: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_reporte_sin_borrado ON reporte ^^^
CREATE TRIGGER trg_reporte_sin_borrado BEFORE DELETE ON reporte
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
