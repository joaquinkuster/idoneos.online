-- Inscripcion: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE UNIQUE INDEX IF NOT EXISTS uq_inscripcion_alumno_cohorte_activo
    ON inscripcion (id_alumno, id_cohorte) WHERE baja = false ^^^

CREATE INDEX IF NOT EXISTS ix_inscripcion_cohorte_activo ON inscripcion (id_cohorte) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

-- Inscripciones finales. Una inscripcion Cancelada por falta de pago o Dada de baja es un estado final: no admite ningun cambio
-- (Inscripcion.esInmutable).

-- fn_inscripcion_final(): disparador de modificacion de una inscripcion. Si ya estaba en un estado final, rechaza cualquier cambio.
-- Darla de baja es una baja y no una modificacion, por eso tiene su propio texto.
CREATE OR REPLACE FUNCTION fn_inscripcion_final() RETURNS trigger AS $$
DECLARE
    v_estado text;
    v_alumno text;
BEGIN
    SELECT nombre INTO v_estado FROM estado_inscripcion WHERE id_estado_inscripcion = OLD.id_estado_inscripcion;
    IF v_estado IN ('Cancelada por falta de pago', 'Dada de baja') THEN
        SELECT u.nombre || ' ' || u.apellido INTO v_alumno
            FROM alumno a JOIN usuario u ON u.id_usuario = a.id_usuario WHERE a.id_alumno = OLD.id_alumno;
        IF OLD.baja = false AND NEW.baja = true THEN
            RAISE EXCEPTION '%', fn_msg_baja('la inscripción de', v_alumno, 'está cancelada por falta de pago, un estado final', '')
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'la inscripción de', v_alumno,
                CASE WHEN v_estado = 'Cancelada por falta de pago' THEN 'está cancelada por falta de pago' ELSE 'está dada de baja' END, '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_inscripcion_final ON inscripcion ^^^
CREATE TRIGGER trg_inscripcion_final BEFORE UPDATE ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_inscripcion_final() ^^^

DROP TRIGGER IF EXISTS trg_inscripcion_inmutable ON inscripcion ^^^
CREATE TRIGGER trg_inscripcion_inmutable BEFORE UPDATE ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_cohorte', 'id_alumno') ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- Cupo de la cohorte: una inscripcion habilitada.
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

-- ----------------------------------------------------------------------
-- Cascadas de baja
-- ----------------------------------------------------------------------

-- fn_cascada_inscripcion(): al dar de baja una inscripcion se anulan sus certificados y se dan de baja sus intentos de autoevaluacion.
-- Sus consultas del foro se conservan: se muestran en forma anonima.
CREATE OR REPLACE FUNCTION fn_cascada_inscripcion() RETURNS trigger AS $$
BEGIN
    UPDATE certificado SET anulado = true, ultima_modificacion = now() WHERE id_inscripcion = NEW.id_inscripcion AND anulado = false;
    UPDATE intento_autoevaluacion SET baja = true, ultima_modificacion = now() WHERE id_inscripcion = NEW.id_inscripcion AND baja = false;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_inscripcion_cascada ON inscripcion ^^^
CREATE TRIGGER trg_inscripcion_cascada AFTER UPDATE OF baja ON inscripcion
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_cascada_inscripcion() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_inscripcion_sin_borrado ON inscripcion ^^^
CREATE TRIGGER trg_inscripcion_sin_borrado BEFORE DELETE ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
