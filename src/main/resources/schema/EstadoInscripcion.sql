-- EstadoInscripcion: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO estado_inscripcion (nombre) VALUES
        ('Pendiente de pago'),
        ('Pago en curso'),
        ('Acceso habilitado'),
        ('Acceso suspendido'),
        ('Acceso vencido'),
        ('Cancelada por falta de pago'),
        ('Dada de baja')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_estado_inscripcion_predefinido ON estado_inscripcion ^^^
CREATE TRIGGER trg_estado_inscripcion_predefinido BEFORE INSERT OR UPDATE OR DELETE ON estado_inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_estado_inscripcion_sin_borrado ON estado_inscripcion ^^^
CREATE TRIGGER trg_estado_inscripcion_sin_borrado BEFORE DELETE ON estado_inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
