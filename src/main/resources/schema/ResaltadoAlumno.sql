-- ResaltadoAlumno: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_resaltado_alumno_pertenencia ON resaltado_alumno ^^^
CREATE TRIGGER trg_resaltado_alumno_pertenencia BEFORE UPDATE ON resaltado_alumno
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_alumno', 'id_documento', 'pagina') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_resaltado_alumno_sin_borrado ON resaltado_alumno ^^^
CREATE TRIGGER trg_resaltado_alumno_sin_borrado BEFORE DELETE ON resaltado_alumno
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_resaltado_alumno_auditoria ON resaltado_alumno ^^^
CREATE TRIGGER trg_resaltado_alumno_auditoria AFTER INSERT OR UPDATE OR DELETE ON resaltado_alumno
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('ResaltadoAlumno') ^^^
