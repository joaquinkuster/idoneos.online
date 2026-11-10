-- Pool: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_pool() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.nombre) = '' THEN
            v_mensaje := fn_msg_vacio('El nombre del pool');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pool_validar ON pool ^^^
CREATE TRIGGER trg_pool_validar BEFORE INSERT OR UPDATE ON pool
    FOR EACH ROW EXECUTE FUNCTION fn_validar_pool() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_pool_tema_activa ON pool ^^^
CREATE TRIGGER trg_pool_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON pool
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_pool_congelado(): disparador de modificacion de un pool. Se llama solo si cambio el nombre o el tema, y rechaza el cambio si el
-- pool tiene intentos activos.
CREATE OR REPLACE FUNCTION fn_pool_congelado() RETURNS trigger AS $$
BEGIN
    IF fn_pool_con_intentos(OLD.id_pool) THEN
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el contenido del pool', OLD.nombre, 'tiene intentos de autoevaluación asociados', '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pool_congelado ON pool ^^^
CREATE TRIGGER trg_pool_congelado BEFORE UPDATE ON pool
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre OR OLD.id_tema IS DISTINCT FROM NEW.id_tema)
    EXECUTE FUNCTION fn_pool_congelado() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_pool_con_preguntas ON pool ^^^
CREATE CONSTRAINT TRIGGER trg_pool_con_preguntas AFTER INSERT OR UPDATE ON pool
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pool_con_preguntas() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_pool(): disparador de baja de un pool. Rechaza la baja si el pool esta asociado a una autoevaluacion activa
-- (una autoevaluacion dada de baja no cuenta).
CREATE OR REPLACE FUNCTION fn_baja_pool() RETURNS trigger AS $$
DECLARE
    v_autoevaluaciones bigint;
BEGIN
    SELECT count(*) INTO v_autoevaluaciones FROM autoevaluacion_pool ap
        JOIN autoevaluacion a ON a.id_autoevaluacion = ap.id_autoevaluacion
        WHERE ap.id_pool = OLD.id_pool AND a.baja = false;
    IF v_autoevaluaciones > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('el pool', OLD.nombre,
                'está asociado a ' || fn_msg_cantidad(v_autoevaluaciones, 'autoevaluación activa', 'autoevaluaciones activas'),
                'Primero hay que dar de baja las autoevaluaciones que lo usan.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pool_baja ON pool ^^^
CREATE TRIGGER trg_pool_baja BEFORE UPDATE OF baja ON pool
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_pool() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_pool_pertenencia ON pool ^^^
CREATE TRIGGER trg_pool_pertenencia BEFORE UPDATE ON pool
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_pool_sin_borrado ON pool ^^^
CREATE TRIGGER trg_pool_sin_borrado BEFORE DELETE ON pool
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
