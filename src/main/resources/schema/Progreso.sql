-- Progreso: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_progreso_pertenencia ON progreso ^^^
CREATE TRIGGER trg_progreso_pertenencia BEFORE UPDATE ON progreso
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema', 'id_inscripcion') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_progreso_sin_borrado ON progreso ^^^
CREATE TRIGGER trg_progreso_sin_borrado BEFORE DELETE ON progreso
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
