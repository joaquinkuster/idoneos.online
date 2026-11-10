-- PreguntaIntento: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_pregunta_intento_pertenencia ON pregunta_intento ^^^
CREATE TRIGGER trg_pregunta_intento_pertenencia BEFORE UPDATE ON pregunta_intento
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_intento_autoevaluacion', 'id_pregunta') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_pregunta_intento_sin_borrado ON pregunta_intento ^^^
CREATE TRIGGER trg_pregunta_intento_sin_borrado BEFORE DELETE ON pregunta_intento
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
