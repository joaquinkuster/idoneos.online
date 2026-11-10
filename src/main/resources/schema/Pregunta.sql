-- Pregunta: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_pregunta() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.texto) = '' THEN
            v_mensaje := fn_msg_vacio('El texto de la pregunta');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pregunta_validar ON pregunta ^^^
CREATE TRIGGER trg_pregunta_validar BEFORE INSERT OR UPDATE ON pregunta
    FOR EACH ROW EXECUTE FUNCTION fn_validar_pregunta() ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_pregunta_congelada(): disparador de las preguntas de un pool.
CREATE OR REPLACE FUNCTION fn_pregunta_congelada() RETURNS trigger AS $$
DECLARE
    v_pool int;
    v_nombre text;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_pool IS NOT DISTINCT FROM NEW.id_pool
       AND OLD.texto IS NOT DISTINCT FROM NEW.texto
       AND OLD.es_opcion_multiple IS NOT DISTINCT FROM NEW.es_opcion_multiple
       AND OLD.baja IS NOT DISTINCT FROM NEW.baja THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'DELETE' THEN v_pool := OLD.id_pool; ELSE v_pool := NEW.id_pool; END IF;
    IF fn_pool_con_intentos(v_pool) THEN
        SELECT nombre INTO v_nombre FROM pool WHERE id_pool = v_pool;
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el contenido del pool', v_nombre, 'tiene intentos de autoevaluación asociados', '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pregunta_congelado ON pregunta ^^^
CREATE TRIGGER trg_pregunta_congelado BEFORE INSERT OR UPDATE OR DELETE ON pregunta
    FOR EACH ROW EXECUTE FUNCTION fn_pregunta_congelada() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_pregunta_completa ON pregunta ^^^
CREATE CONSTRAINT TRIGGER trg_pregunta_completa AFTER INSERT OR UPDATE ON pregunta
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pregunta_completa() ^^^

DROP TRIGGER IF EXISTS trg_pregunta_pool_con_preguntas ON pregunta ^^^
CREATE CONSTRAINT TRIGGER trg_pregunta_pool_con_preguntas AFTER INSERT OR UPDATE OR DELETE ON pregunta
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pool_con_preguntas() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_pregunta_pertenencia ON pregunta ^^^
CREATE TRIGGER trg_pregunta_pertenencia BEFORE UPDATE ON pregunta
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_pool') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_pregunta_sin_borrado ON pregunta ^^^
CREATE TRIGGER trg_pregunta_sin_borrado BEFORE DELETE ON pregunta
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
