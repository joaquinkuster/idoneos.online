-- CursoModalidad: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_curso_modalidad_congelada(): disparador de las modalidades de un curso.
CREATE OR REPLACE FUNCTION fn_curso_modalidad_congelada() RETURNS trigger AS $$
DECLARE
    v_curso int;
    v_nombre text;
BEGIN
    IF TG_OP = 'DELETE' THEN v_curso := OLD.id_curso; ELSE v_curso := NEW.id_curso; END IF;
    IF fn_curso_tiene_cohortes_publicadas(v_curso) THEN
        SELECT nombre INTO v_nombre FROM curso WHERE id_curso = v_curso;
        RAISE EXCEPTION '%', fn_msg_publicacion('el curso', v_nombre, fn_txt_motivo_publicado(), fn_txt_cierre_curso())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_modalidad_congelado ON curso_modalidad ^^^
CREATE TRIGGER trg_curso_modalidad_congelado BEFORE INSERT OR UPDATE OR DELETE ON curso_modalidad
    FOR EACH ROW EXECUTE FUNCTION fn_curso_modalidad_congelada() ^^^
