-- IntentoAutoevaluacion: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_intento_autoevaluacion_autoevaluacion_activo ON intento_autoevaluacion (id_autoevaluacion) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_intento_autoevaluacion_autoevaluacion_activa ON intento_autoevaluacion ^^^
CREATE TRIGGER trg_intento_autoevaluacion_autoevaluacion_activa BEFORE INSERT OR UPDATE OF id_autoevaluacion, baja ON intento_autoevaluacion
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_autoevaluacion', 'autoevaluacion', 'id_autoevaluacion') ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_intento_autoevaluacion_pertenencia ON intento_autoevaluacion ^^^
CREATE TRIGGER trg_intento_autoevaluacion_pertenencia BEFORE UPDATE ON intento_autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_autoevaluacion', 'id_inscripcion') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_intento_autoevaluacion_sin_borrado ON intento_autoevaluacion ^^^
CREATE TRIGGER trg_intento_autoevaluacion_sin_borrado BEFORE DELETE ON intento_autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
