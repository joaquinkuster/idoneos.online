-- EvaluacionUtil Intentos de las autoevaluaciones y estructura de pools y preguntas.

-- Autoevaluaciones rendidas. Lo que un alumno rindio no se modifica: un pool o una autoevaluacion con intentos activos
-- (un intento dado de baja por fraude no cuenta) queda congelado.

-- fn_autoevaluacion_con_intentos(p_id_autoevaluacion): funcion auxiliar. Devuelve true si la autoevaluacion tiene algun
-- intento activo.
CREATE OR REPLACE FUNCTION fn_autoevaluacion_con_intentos(p_id_autoevaluacion int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM intento_autoevaluacion
                   WHERE id_autoevaluacion = p_id_autoevaluacion AND baja = false)
$$ LANGUAGE sql STABLE ^^^

-- fn_pool_con_intentos(p_id_pool): funcion auxiliar.
CREATE OR REPLACE FUNCTION fn_pool_con_intentos(p_id_pool int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM intento_autoevaluacion i
                   JOIN autoevaluacion a ON a.id_autoevaluacion = i.id_autoevaluacion
                   JOIN autoevaluacion_pool ap ON ap.id_autoevaluacion = a.id_autoevaluacion
                   WHERE ap.id_pool = p_id_pool AND a.baja = false AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

-- Estructura de las preguntas: cada pregunta activa tiene al menos dos opciones activas y.
CREATE OR REPLACE FUNCTION fn_pregunta_completa() RETURNS trigger AS $$
DECLARE
    v_pregunta int;
    v_multiple boolean;
    v_baja boolean;
    v_total bigint;
    v_correctas bigint;
BEGIN
    IF TG_TABLE_NAME = 'opcion_respuesta' THEN
        IF TG_OP = 'DELETE' THEN v_pregunta := OLD.id_pregunta; ELSE v_pregunta := NEW.id_pregunta; END IF;
    ELSE
        IF TG_OP = 'DELETE' THEN RETURN NULL; END IF;
        v_pregunta := NEW.id_pregunta;
    END IF;
    SELECT es_opcion_multiple, baja INTO v_multiple, v_baja FROM pregunta WHERE id_pregunta = v_pregunta;
    IF NOT FOUND OR v_baja THEN
        RETURN NULL;
    END IF;
    SELECT count(*), count(*) FILTER (WHERE es_correcta) INTO v_total, v_correctas
        FROM opcion_respuesta WHERE id_pregunta = v_pregunta AND baja = false;
    IF v_total < 2 THEN
        RAISE EXCEPTION 'La pregunta debe tener al menos dos opciones'
            USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF (v_multiple AND v_correctas < 1) OR (NOT v_multiple AND v_correctas <> 1) THEN
        RAISE EXCEPTION 'La pregunta debe tener %', CASE WHEN v_multiple THEN 'al menos una opcion correcta' ELSE 'exactamente una opcion correcta' END
            USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

-- fn_pool_con_preguntas(): se ejecuta al terminar la operacion (disparador diferido) cuando cambia un pool o una de sus
-- preguntas. Verifica que el pool activo conserve al menos una pregunta activa.
CREATE OR REPLACE FUNCTION fn_pool_con_preguntas() RETURNS trigger AS $$
DECLARE
    v_pool int;
    v_baja boolean;
BEGIN
    IF TG_TABLE_NAME = 'pool' THEN
        v_pool := NEW.id_pool;
    ELSIF TG_OP = 'DELETE' THEN
        v_pool := OLD.id_pool;
    ELSE
        v_pool := NEW.id_pool;
    END IF;
    SELECT baja INTO v_baja FROM pool WHERE id_pool = v_pool;
    IF NOT FOUND OR v_baja THEN
        RETURN NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pregunta WHERE id_pool = v_pool AND baja = false) THEN
        RAISE EXCEPTION 'El pool debe tener al menos una pregunta'
            USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^
