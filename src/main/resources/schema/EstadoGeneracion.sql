-- EstadoGeneracion: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO estado_generacion (nombre) VALUES
        ('Pendiente'),
        ('Generando'),
        ('Generado'),
        ('Error')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_estado_generacion_predefinido ON estado_generacion ^^^
CREATE TRIGGER trg_estado_generacion_predefinido BEFORE INSERT OR UPDATE OR DELETE ON estado_generacion
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_estado_generacion_sin_borrado ON estado_generacion ^^^
CREATE TRIGGER trg_estado_generacion_sin_borrado BEFORE DELETE ON estado_generacion
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
