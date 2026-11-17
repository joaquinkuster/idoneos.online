-- Alumno: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_alumno_pertenencia ON alumno ^^^
CREATE TRIGGER trg_alumno_pertenencia BEFORE UPDATE ON alumno
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_usuario') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_alumno_sin_borrado ON alumno ^^^
CREATE TRIGGER trg_alumno_sin_borrado BEFORE DELETE ON alumno
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_alumno_auditoria ON alumno ^^^
CREATE TRIGGER trg_alumno_auditoria AFTER INSERT OR UPDATE OR DELETE ON alumno
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('Alumno') ^^^
