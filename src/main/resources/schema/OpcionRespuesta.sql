-- OpcionRespuesta: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_opcion_respuesta() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.texto) = '' THEN
            v_mensaje := fn_msg_vacio('El texto de la opción');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_opcion_respuesta_validar ON opcion_respuesta ^^^
CREATE TRIGGER trg_opcion_respuesta_validar BEFORE INSERT OR UPDATE ON opcion_respuesta
    FOR EACH ROW EXECUTE FUNCTION fn_validar_opcion_respuesta() ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_opcion_respuesta_congelada(): disparador de las opciones de respuesta. Busca el pool de la pregunta y rechaza agregar,
-- modificar o dar de baja una opcion cuando ese pool tiene intentos activos.
CREATE OR REPLACE FUNCTION fn_opcion_respuesta_congelada() RETURNS trigger AS $$
DECLARE
    v_pregunta int;
    v_pool int;
    v_nombre text;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_pregunta IS NOT DISTINCT FROM NEW.id_pregunta
       AND OLD.texto IS NOT DISTINCT FROM NEW.texto
       AND OLD.es_correcta IS NOT DISTINCT FROM NEW.es_correcta
       AND OLD.baja IS NOT DISTINCT FROM NEW.baja THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'DELETE' THEN v_pregunta := OLD.id_pregunta; ELSE v_pregunta := NEW.id_pregunta; END IF;
    SELECT id_pool INTO v_pool FROM pregunta WHERE id_pregunta = v_pregunta;
    IF fn_pool_con_intentos(v_pool) THEN
        SELECT nombre INTO v_nombre FROM pool WHERE id_pool = v_pool;
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el contenido del pool', v_nombre, 'tiene intentos de autoevaluación asociados', '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_opcion_respuesta_congelado ON opcion_respuesta ^^^
CREATE TRIGGER trg_opcion_respuesta_congelado BEFORE INSERT OR UPDATE OR DELETE ON opcion_respuesta
    FOR EACH ROW EXECUTE FUNCTION fn_opcion_respuesta_congelada() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_opcion_completa ON opcion_respuesta ^^^
CREATE CONSTRAINT TRIGGER trg_opcion_completa AFTER INSERT OR UPDATE OR DELETE ON opcion_respuesta
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pregunta_completa() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_opcion_respuesta_pertenencia ON opcion_respuesta ^^^
CREATE TRIGGER trg_opcion_respuesta_pertenencia BEFORE UPDATE ON opcion_respuesta
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_pregunta') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_opcion_respuesta_sin_borrado ON opcion_respuesta ^^^
CREATE TRIGGER trg_opcion_respuesta_sin_borrado BEFORE DELETE ON opcion_respuesta
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
