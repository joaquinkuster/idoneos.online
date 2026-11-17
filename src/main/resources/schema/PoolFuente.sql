-- PoolFuente: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las fuentes de un banco de preguntas no se borran: dejan registrado de dónde salió.
DROP TRIGGER IF EXISTS trg_pool_fuente_sin_borrado ON pool_fuente ^^^
CREATE TRIGGER trg_pool_fuente_sin_borrado BEFORE DELETE ON pool_fuente
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_pool_fuente_auditoria ON pool_fuente ^^^
CREATE TRIGGER trg_pool_fuente_auditoria AFTER INSERT OR UPDATE OR DELETE ON pool_fuente
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('PoolFuente') ^^^
