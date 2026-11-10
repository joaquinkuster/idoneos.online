-- Recurso: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_recurso() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.titulo) = '' THEN
            v_mensaje := fn_msg_vacio('El título del recurso');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_recurso_validar ON recurso ^^^
CREATE TRIGGER trg_recurso_validar BEFORE INSERT OR UPDATE ON recurso
    FOR EACH ROW EXECUTE FUNCTION fn_validar_recurso() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_recurso_en_uso ON recurso ^^^
CREATE TRIGGER trg_recurso_en_uso BEFORE UPDATE ON recurso
    FOR EACH ROW WHEN (OLD.titulo IS DISTINCT FROM NEW.titulo
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion)
    EXECUTE FUNCTION fn_recurso_en_uso() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_recurso(): un recurso no se da de baja si algun tema lo usa (hay que dar de baja primero su uso en cada tema).
CREATE OR REPLACE FUNCTION fn_baja_recurso() RETURNS trigger AS $$
DECLARE
    v_usos bigint;
BEGIN
    SELECT count(*) INTO v_usos FROM tema_recurso WHERE id_recurso = OLD.id_recurso AND baja = false;
    IF v_usos > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('el recurso', OLD.titulo,
                'tiene ' || fn_msg_cantidad(v_usos, 'tema asociado', 'temas asociados'),
                'Dé de baja primero su recurso en ' || CASE WHEN v_usos = 1 THEN 'ese tema.' ELSE 'esos temas.' END)
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_recurso_baja ON recurso ^^^
CREATE TRIGGER trg_recurso_baja BEFORE UPDATE OF baja ON recurso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_recurso() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_recurso_pertenencia ON recurso ^^^
CREATE TRIGGER trg_recurso_pertenencia BEFORE UPDATE ON recurso
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_docente') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_recurso_sin_borrado ON recurso ^^^
CREATE TRIGGER trg_recurso_sin_borrado BEFORE DELETE ON recurso
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
