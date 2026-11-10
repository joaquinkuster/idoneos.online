-- RecordatorioCohorte: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_recordatorio_cohorte_pertenencia ON recordatorio_cohorte ^^^
CREATE TRIGGER trg_recordatorio_cohorte_pertenencia BEFORE UPDATE ON recordatorio_cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_alumno', 'id_cohorte') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_recordatorio_cohorte_sin_borrado ON recordatorio_cohorte ^^^
CREATE TRIGGER trg_recordatorio_cohorte_sin_borrado BEFORE DELETE ON recordatorio_cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
