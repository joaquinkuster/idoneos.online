-- EstadoAvance: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO estado_avance (nombre) VALUES
        ('En progreso'),
        ('Finalizado')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_estado_avance_predefinido ON estado_avance ^^^
CREATE TRIGGER trg_estado_avance_predefinido BEFORE INSERT OR UPDATE OR DELETE ON estado_avance
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_estado_avance_sin_borrado ON estado_avance ^^^
CREATE TRIGGER trg_estado_avance_sin_borrado BEFORE DELETE ON estado_avance
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
