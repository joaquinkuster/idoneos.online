-- InteresAlumno: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_interes_alumno_pertenencia ON interes_alumno ^^^
CREATE TRIGGER trg_interes_alumno_pertenencia BEFORE UPDATE ON interes_alumno
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_alumno', 'id_categoria') ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_interes_alumno_auditoria ON interes_alumno ^^^
CREATE TRIGGER trg_interes_alumno_auditoria AFTER INSERT OR UPDATE OR DELETE ON interes_alumno
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('InteresAlumno') ^^^
