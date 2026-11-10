-- Rol: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO rol (nombre) VALUES
        ('Administrador'),
        ('Docente'),
        ('Alumno')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_rol_predefinido ON rol ^^^
CREATE TRIGGER trg_rol_predefinido BEFORE INSERT OR UPDATE OR DELETE ON rol
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_rol_sin_borrado ON rol ^^^
CREATE TRIGGER trg_rol_sin_borrado BEFORE DELETE ON rol
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
