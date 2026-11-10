-- DocenteCurso: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE UNIQUE INDEX IF NOT EXISTS uq_docente_curso_curso_docente_activo
    ON docente_curso (id_curso, id_docente) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_docente_curso_curso_activo ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON docente_curso
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_docente_curso(): un docente no se desvincula de un curso si tiene clases o material activos en el.
CREATE OR REPLACE FUNCTION fn_baja_docente_curso() RETURNS trigger AS $$
DECLARE
    v_nombre text;
BEGIN
    IF EXISTS (SELECT 1 FROM clase_en_vivo WHERE id_docente_curso = OLD.id_docente_curso AND baja = false)
       OR EXISTS (SELECT 1 FROM tema_recurso WHERE id_docente_curso = OLD.id_docente_curso AND baja = false) THEN
        SELECT u.nombre || ' ' || u.apellido INTO v_nombre
            FROM docente d JOIN usuario u ON u.id_usuario = d.id_usuario WHERE d.id_docente = OLD.id_docente;
        RAISE EXCEPTION 'Error! El docente % tiene clases y/o material activo en el curso. Dé de baja primero sus clases y materiales para poder desvincularlo.', v_nombre
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_docente_curso_baja ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_baja BEFORE UPDATE OF baja ON docente_curso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_docente_curso() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_docente_curso_pertenencia ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_pertenencia BEFORE UPDATE ON docente_curso
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_curso', 'id_docente') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_docente_curso_sin_borrado ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_sin_borrado BEFORE DELETE ON docente_curso
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
