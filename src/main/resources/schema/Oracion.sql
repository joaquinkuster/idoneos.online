-- Oracion: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Las oraciones de un documento no se editan: si cambia su índice o su archivo, se borran y se vuelven a armar. Por eso tampoco se bloquea su borrado.
DROP TRIGGER IF EXISTS trg_oracion_pertenencia ON oracion ^^^
CREATE TRIGGER trg_oracion_pertenencia BEFORE UPDATE OF texto ON oracion
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('texto') ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_oracion_auditoria ON oracion ^^^
CREATE TRIGGER trg_oracion_auditoria AFTER INSERT OR UPDATE OR DELETE ON oracion
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('Oracion') ^^^
