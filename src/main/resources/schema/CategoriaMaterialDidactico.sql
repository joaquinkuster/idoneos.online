-- CategoriaMaterialDidactico: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO categoria_material_didactico (nombre) VALUES
        ('Presentación'),
        ('Apunte de Cátedra'),
        ('Guía de Estudio')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_categoria_material_didactico_predefinido ON categoria_material_didactico ^^^
CREATE TRIGGER trg_categoria_material_didactico_predefinido BEFORE INSERT OR UPDATE OR DELETE ON categoria_material_didactico
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_categoria_material_didactico_sin_borrado ON categoria_material_didactico ^^^
CREATE TRIGGER trg_categoria_material_didactico_sin_borrado BEFORE DELETE ON categoria_material_didactico
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
