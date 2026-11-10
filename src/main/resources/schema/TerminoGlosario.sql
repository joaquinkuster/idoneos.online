-- TerminoGlosario: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_termino_glosario() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.termino) = '' THEN
            v_mensaje := fn_msg_vacio('El término');
        ELSIF btrim(NEW.definicion) = '' THEN
            v_mensaje := fn_msg_vacio('La definición');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_termino_glosario_validar ON termino_glosario ^^^
CREATE TRIGGER trg_termino_glosario_validar BEFORE INSERT OR UPDATE ON termino_glosario
    FOR EACH ROW EXECUTE FUNCTION fn_validar_termino_glosario() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_termino_glosario_tema_activa ON termino_glosario ^^^
CREATE TRIGGER trg_termino_glosario_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON termino_glosario
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_termino_glosario_pertenencia ON termino_glosario ^^^
CREATE TRIGGER trg_termino_glosario_pertenencia BEFORE UPDATE ON termino_glosario
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_termino_glosario_sin_borrado ON termino_glosario ^^^
CREATE TRIGGER trg_termino_glosario_sin_borrado BEFORE DELETE ON termino_glosario
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
