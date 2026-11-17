-- DocumentoFuente: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las fuentes de un material generado no se borran: dejan registrado de dónde salió.
DROP TRIGGER IF EXISTS trg_documento_fuente_sin_borrado ON documento_fuente ^^^
CREATE TRIGGER trg_documento_fuente_sin_borrado BEFORE DELETE ON documento_fuente
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_documento_fuente_auditoria ON documento_fuente ^^^
CREATE TRIGGER trg_documento_fuente_auditoria AFTER INSERT OR UPDATE OR DELETE ON documento_fuente
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('DocumentoFuente') ^^^
