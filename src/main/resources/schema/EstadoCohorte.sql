-- EstadoCohorte: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Valores predefinidos
-- ----------------------------------------------------------------------

DO $do$
BEGIN
    PERFORM set_config('idoneos.carga_inicial', 'on', true);
    INSERT INTO estado_cohorte (nombre) VALUES
        ('Pendiente de contenido'),
        ('Inscripción próxima'),
        ('Inscripción abierta'),
        ('Inscripción cerrada'),
        ('En dictado'),
        ('Dictado finalizado'),
        ('Cursada finalizada'),
        ('Cancelada por falta de contenido')
    ON CONFLICT (nombre) DO NOTHING;
END
$do$ ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- Es un valor predefinido: solo la carga inicial lo agrega, modifica o borra.
DROP TRIGGER IF EXISTS trg_estado_cohorte_predefinido ON estado_cohorte ^^^
CREATE TRIGGER trg_estado_cohorte_predefinido BEFORE INSERT OR UPDATE OR DELETE ON estado_cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_catalogo_predefinido() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_estado_cohorte_sin_borrado ON estado_cohorte ^^^
CREATE TRIGGER trg_estado_cohorte_sin_borrado BEFORE DELETE ON estado_cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
