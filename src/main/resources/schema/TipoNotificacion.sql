-- TipoNotificacion: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO tipo_notificacion (nombre) VALUES
        ('Cohorte activada'),
        ('Cohorte cancelada'),
        ('Recordatorio de cohorte')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_tipo_notificacion_predefinido ON tipo_notificacion ^^^
CREATE TRIGGER trg_tipo_notificacion_predefinido BEFORE INSERT OR UPDATE OR DELETE ON tipo_notificacion
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_tipo_notificacion_sin_borrado ON tipo_notificacion ^^^
CREATE TRIGGER trg_tipo_notificacion_sin_borrado BEFORE DELETE ON tipo_notificacion
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
