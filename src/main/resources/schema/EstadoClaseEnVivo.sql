-- EstadoClaseEnVivo: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO estado_clase_en_vivo (nombre) VALUES
        ('Programada'),
        ('En vivo'),
        ('Finalizada'),
        ('Cancelada')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_estado_clase_en_vivo_predefinido ON estado_clase_en_vivo ^^^
CREATE TRIGGER trg_estado_clase_en_vivo_predefinido BEFORE INSERT OR UPDATE OR DELETE ON estado_clase_en_vivo
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_estado_clase_en_vivo_sin_borrado ON estado_clase_en_vivo ^^^
CREATE TRIGGER trg_estado_clase_en_vivo_sin_borrado BEFORE DELETE ON estado_clase_en_vivo
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
