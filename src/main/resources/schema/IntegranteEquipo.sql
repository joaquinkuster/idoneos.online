-- IntegranteEquipo: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE UNIQUE INDEX IF NOT EXISTS uq_integrante_equipo_curso_docente_activo
    ON integrante_equipo (id_curso, id_docente) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_integrante_equipo_curso_activo ON integrante_equipo ^^^
CREATE TRIGGER trg_integrante_equipo_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON integrante_equipo
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_integrante_equipo(): un docente no se desvincula de un curso si tiene clases o material activos en el.
CREATE OR REPLACE FUNCTION fn_baja_integrante_equipo() RETURNS trigger AS $$
DECLARE
    v_nombre text;
BEGIN
    IF EXISTS (SELECT 1 FROM clase_en_vivo WHERE id_integrante_equipo = OLD.id_integrante_equipo AND baja = false)
       OR EXISTS (SELECT 1 FROM tema_recurso WHERE id_integrante_equipo = OLD.id_integrante_equipo AND baja = false) THEN
        SELECT u.nombre || ' ' || u.apellido INTO v_nombre
            FROM docente d JOIN usuario u ON u.id_usuario = d.id_usuario WHERE d.id_docente = OLD.id_docente;
        RAISE EXCEPTION 'Error! El docente % tiene clases y/o material activo en el curso. Dé de baja primero sus clases y materiales para poder desvincularlo.', v_nombre
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_integrante_equipo_baja ON integrante_equipo ^^^
CREATE TRIGGER trg_integrante_equipo_baja BEFORE UPDATE OF baja ON integrante_equipo
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_integrante_equipo() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_integrante_equipo_pertenencia ON integrante_equipo ^^^
CREATE TRIGGER trg_integrante_equipo_pertenencia BEFORE UPDATE ON integrante_equipo
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_curso', 'id_docente') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_integrante_equipo_sin_borrado ON integrante_equipo ^^^
CREATE TRIGGER trg_integrante_equipo_sin_borrado BEFORE DELETE ON integrante_equipo
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
