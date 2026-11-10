-- RespuestaIntento: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_respuesta_intento_pertenencia ON respuesta_intento ^^^
CREATE TRIGGER trg_respuesta_intento_pertenencia BEFORE UPDATE ON respuesta_intento
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_pregunta_intento', 'id_opcion_respuesta') ^^^
