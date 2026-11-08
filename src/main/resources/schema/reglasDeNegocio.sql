-- Reglas de negocio de varias filas (solo PostgreSQL): cupo de la cohorte, estructura de las preguntas y pago aprobado.
-- Spring ejecuta todos los scripts de esta carpeta (ver application.properties) despues de que Hibernate crea las tablas.
-- Cada archivo define sus propias funciones, por eso el orden entre ellos no importa.
-- Cada sentencia termina con ^^^ (y no con punto y coma) porque los cuerpos de las funciones llevan punto y coma adentro.
-- Se puede ejecutar mas de una vez: usa IF NOT EXISTS, CREATE OR REPLACE y DROP ... IF EXISTS.

-- ---------------------------------------------------------------------------------------------
-- Cupo de la cohorte (CU-45 y CU-46): una inscripcion habilitada, suspendida o con un pago en curso ocupa un cupo.
-- Al pasar a uno de esos estados se bloquea la cohorte mientras se cuentan los cupos ocupados. La aplicacion ya bloquea la
-- cohorte antes de decidir; este disparador es la ultima barrera de la base.
-- ---------------------------------------------------------------------------------------------
-- fn_cupo_cohorte(): disparador de las inscripciones. Cuando una inscripcion pasa a ocupar un lugar, bloquea la fila de la
-- cohorte (FOR UPDATE) y cuenta las inscripciones activas que ya lo ocupan; si ya se alcanzo el cupo maximo, la rechaza.
-- Una cohorte sin cupo maximo no tiene limite.
CREATE OR REPLACE FUNCTION fn_cupo_cohorte() RETURNS trigger AS $$
DECLARE
    v_cupo int;
    v_ocupados bigint;
    v_ocupa boolean;
    v_ocupaba boolean := false;
BEGIN
    SELECT nombre IN ('Acceso habilitado', 'Acceso suspendido', 'Acceso vencido', 'Pago en curso') INTO v_ocupa
        FROM estado_inscripcion WHERE id_estado_inscripcion = NEW.id_estado_inscripcion;
    IF v_ocupa IS NOT TRUE OR NEW.baja = true THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'UPDATE' AND OLD.baja = false AND OLD.id_cohorte = NEW.id_cohorte THEN
        SELECT nombre IN ('Acceso habilitado', 'Acceso suspendido', 'Acceso vencido', 'Pago en curso') INTO v_ocupaba
            FROM estado_inscripcion WHERE id_estado_inscripcion = OLD.id_estado_inscripcion;
        IF v_ocupaba IS TRUE THEN
            RETURN NEW;
        END IF;
    END IF;
    SELECT cupo_maximo INTO v_cupo FROM cohorte WHERE id_cohorte = NEW.id_cohorte FOR UPDATE;
    IF v_cupo IS NULL THEN
        RETURN NEW;
    END IF;
    SELECT count(*) INTO v_ocupados FROM inscripcion i
        JOIN estado_inscripcion e ON e.id_estado_inscripcion = i.id_estado_inscripcion
        WHERE i.id_cohorte = NEW.id_cohorte AND i.baja = false AND e.nombre IN ('Acceso habilitado', 'Acceso suspendido', 'Acceso vencido', 'Pago en curso')
            AND i.id_inscripcion <> NEW.id_inscripcion;
    IF v_ocupados >= v_cupo THEN
        RAISE EXCEPTION 'No se puede ocupar un lugar: la cohorte alcanzo su cupo maximo (%)', v_cupo
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_inscripcion_cupo ON inscripcion ^^^
CREATE TRIGGER trg_inscripcion_cupo BEFORE INSERT OR UPDATE ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_cupo_cohorte() ^^^

-- ---------------------------------------------------------------------------------------------
-- Estructura de las preguntas (CU-61 y CU-62): cada pregunta activa tiene al menos dos opciones activas y, si es
-- de opcion unica, exactamente una correcta (si es multiple, al menos una). Cada pool activo tiene al menos una
-- pregunta activa. Se controla al terminar la operacion (disparadores diferidos), porque la pregunta y sus
-- opciones se guardan juntas.
-- ---------------------------------------------------------------------------------------------
-- fn_pregunta_completa(): se ejecuta al terminar la operacion (disparador diferido) cada vez que cambia una pregunta o una de
-- sus opciones. Verifica que la pregunta activa tenga al menos dos opciones activas y que la cantidad de correctas sea la
-- que corresponde: exactamente una si es de opcion unica, al menos una si es de opcion multiple.
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

DROP TRIGGER IF EXISTS trg_pregunta_completa ON pregunta ^^^
CREATE CONSTRAINT TRIGGER trg_pregunta_completa AFTER INSERT OR UPDATE ON pregunta
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pregunta_completa() ^^^

DROP TRIGGER IF EXISTS trg_opcion_completa ON opcion_respuesta ^^^
CREATE CONSTRAINT TRIGGER trg_opcion_completa AFTER INSERT OR UPDATE OR DELETE ON opcion_respuesta
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pregunta_completa() ^^^

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

DROP TRIGGER IF EXISTS trg_pool_con_preguntas ON pool ^^^
CREATE CONSTRAINT TRIGGER trg_pool_con_preguntas AFTER INSERT OR UPDATE ON pool
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pool_con_preguntas() ^^^

DROP TRIGGER IF EXISTS trg_pregunta_pool_con_preguntas ON pregunta ^^^
CREATE CONSTRAINT TRIGGER trg_pregunta_pool_con_preguntas AFTER INSERT OR UPDATE OR DELETE ON pregunta
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_pool_con_preguntas() ^^^

-- ---------------------------------------------------------------------------------------------
-- Pago aprobado (CU-54): un pago aprobado ya no admite cambios de estado, monto, comprobante ni inscripcion.
-- ---------------------------------------------------------------------------------------------
-- fn_pago_aprobado() (CU-54): disparador de modificacion de un pago. El disparador solo la llama si cambio el estado, el monto,
-- el comprobante o la inscripcion; si el pago ya estaba aprobado, rechaza el cambio.
CREATE OR REPLACE FUNCTION fn_pago_aprobado() RETURNS trigger AS $$
BEGIN
    IF (SELECT nombre FROM estado_pago WHERE id_estado_pago = OLD.id_estado_pago) = 'Aprobado' THEN
        RAISE EXCEPTION 'No se puede modificar un pago aprobado'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pago_aprobado ON pago ^^^
CREATE TRIGGER trg_pago_aprobado BEFORE UPDATE ON pago
    FOR EACH ROW WHEN (OLD.id_estado_pago IS DISTINCT FROM NEW.id_estado_pago
                    OR OLD.monto IS DISTINCT FROM NEW.monto
                    OR OLD.numero_comprobante IS DISTINCT FROM NEW.numero_comprobante
                    OR OLD.id_inscripcion IS DISTINCT FROM NEW.id_inscripcion)
    EXECUTE FUNCTION fn_pago_aprobado() ^^^
