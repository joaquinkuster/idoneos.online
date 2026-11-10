-- IndiceRecurso: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_indice_recurso() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.titulo) = '' THEN
            v_mensaje := fn_msg_vacio('El título de la entrada del índice');
        ELSIF NEW.nivel < 1 OR NEW.nivel > 3 THEN
            v_mensaje := fn_msg_formato('El nivel de la entrada del índice', 'número entero de 1 a 3');
        ELSIF NEW.pagina < 1 THEN
            v_mensaje := fn_msg_formato('La página de la entrada del índice', 'número entero desde 1');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_indice_recurso_validar ON indice_recurso ^^^
CREATE TRIGGER trg_indice_recurso_validar BEFORE INSERT OR UPDATE ON indice_recurso
    FOR EACH ROW EXECUTE FUNCTION fn_validar_indice_recurso() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_indice_recurso_pertenencia ON indice_recurso ^^^
CREATE TRIGGER trg_indice_recurso_pertenencia BEFORE UPDATE ON indice_recurso
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_recurso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_indice_recurso_sin_borrado ON indice_recurso ^^^
CREATE TRIGGER trg_indice_recurso_sin_borrado BEFORE DELETE ON indice_recurso
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
