-- Autoevaluacion: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_autoevaluacion_tema_activo ON autoevaluacion (id_tema) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_autoevaluacion() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.nombre) = '' THEN
            v_mensaje := 'Error! Debe completar el nombre de la autoevaluación.';
        ELSIF NEW.tiempo_limite_minutos IS NOT NULL AND NEW.tiempo_limite_minutos <= 0 THEN
            v_mensaje := 'Error! El tiempo límite debe ser un número entero mayor a cero.';
        ELSIF NEW.cantidad_preguntas <= 0 THEN
            v_mensaje := 'Error! La cantidad de preguntas debe ser un número entero mayor a cero.';
        ELSIF NEW.nota_minima <= 0 THEN
            v_mensaje := 'Error! La nota mínima debe ser un número entero mayor a cero.';
        ELSIF NEW.nota_minima > 10 THEN
            v_mensaje := 'Error! La nota mínima no puede ser mayor a 10.';
        ELSIF NEW.intentos_permitidos IS NOT NULL AND NEW.intentos_permitidos <= 0 THEN
            v_mensaje := 'Error! La cantidad de intentos permitidos debe ser un número entero mayor a cero.';
        ELSIF NEW.fecha_cierre IS NOT NULL AND NEW.fecha_cierre <= NEW.fecha_apertura THEN
            v_mensaje := 'Error! La fecha de cierre debe ser posterior a la fecha de apertura.';
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_validar ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_validar BEFORE INSERT OR UPDATE ON autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_validar_autoevaluacion() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_autoevaluacion_tema_activa ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON autoevaluacion
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_autoevaluacion_congelada(): disparador de modificacion de una autoevaluacion.
CREATE OR REPLACE FUNCTION fn_autoevaluacion_congelada() RETURNS trigger AS $$
DECLARE
    v_titulo text;
BEGIN
    IF fn_autoevaluacion_con_intentos(OLD.id_autoevaluacion) THEN
        IF OLD.id_tema IS DISTINCT FROM NEW.id_tema
           OR OLD.nombre IS DISTINCT FROM NEW.nombre
           OR OLD.tiempo_limite_minutos IS DISTINCT FROM NEW.tiempo_limite_minutos
           OR OLD.cantidad_preguntas IS DISTINCT FROM NEW.cantidad_preguntas
           OR OLD.nota_minima IS DISTINCT FROM NEW.nota_minima
           OR date_trunc('minute', OLD.fecha_apertura) IS DISTINCT FROM date_trunc('minute', NEW.fecha_apertura)
           OR OLD.descuento_por_error IS DISTINCT FROM NEW.descuento_por_error
           OR OLD.navegacion_libre IS DISTINCT FROM NEW.navegacion_libre
           OR OLD.permite_salir IS DISTINCT FROM NEW.permite_salir THEN
            RAISE EXCEPTION 'Error! La autoevaluación «%» ya registra intentos: solo pueden modificarse la fecha de cierre, la visibilidad y la cantidad de intentos permitidos.', OLD.nombre
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
        IF NOT (OLD.intentos_permitidos IS NOT DISTINCT FROM NEW.intentos_permitidos
                OR (OLD.intentos_permitidos IS NOT NULL AND (NEW.intentos_permitidos IS NULL OR NEW.intentos_permitidos > OLD.intentos_permitidos))) THEN
            RAISE EXCEPTION 'Error! La cantidad de intentos permitidos solo puede ampliarse (o quedar sin límite) porque la autoevaluación ya registra intentos.'
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
        IF NOT ((OLD.fecha_cierre IS NULL AND NEW.fecha_cierre IS NULL)
                OR (OLD.fecha_cierre IS NOT NULL AND (NEW.fecha_cierre IS NULL
                    OR date_trunc('minute', NEW.fecha_cierre) >= date_trunc('minute', OLD.fecha_cierre)))) THEN
            RAISE EXCEPTION 'Error! La fecha de cierre solo puede extenderse (o eliminarse) porque la autoevaluación ya registra intentos.'
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
    END IF;
    -- Ocultarla: no puede ser la unica visible de un tema que forma parte del cronograma de un programa activo
    IF OLD.oculto = false AND NEW.oculto = true AND NEW.baja = false
       AND EXISTS (SELECT 1 FROM tema_cronograma tc JOIN programa p ON p.id_programa = tc.id_programa
                   WHERE tc.id_tema = OLD.id_tema AND tc.baja = false AND p.baja = false)
       AND NOT EXISTS (SELECT 1 FROM autoevaluacion a
                       WHERE a.id_tema = OLD.id_tema AND a.baja = false AND a.oculto = false
                         AND a.id_autoevaluacion <> OLD.id_autoevaluacion) THEN
        SELECT titulo INTO v_titulo FROM tema WHERE id_tema = OLD.id_tema;
        RAISE EXCEPTION '%', fn_msg_accion('ocultar', 'la autoevaluación', OLD.nombre,
                fn_msg_diseno_incompleto('es la única autoevaluación visible del tema «' || v_titulo || '»'), 'Registrá otra antes de ocultarla.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_congelado ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_congelado BEFORE UPDATE ON autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_autoevaluacion_congelada() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_autoevaluacion(): disparador de baja de una autoevaluacion.
CREATE OR REPLACE FUNCTION fn_baja_autoevaluacion() RETURNS trigger AS $$
DECLARE
    v_intentos bigint;
    v_titulo text;
BEGIN
    SELECT count(*) INTO v_intentos FROM intento_autoevaluacion
        WHERE id_autoevaluacion = OLD.id_autoevaluacion AND baja = false;
    IF v_intentos > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('la autoevaluación', OLD.nombre,
                'tiene ' || fn_msg_cantidad(v_intentos, 'intento registrado', 'intentos registrados'), '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF OLD.oculto = false
       AND EXISTS (SELECT 1 FROM tema_cronograma tc JOIN programa p ON p.id_programa = tc.id_programa
                   WHERE tc.id_tema = OLD.id_tema AND tc.baja = false AND p.baja = false)
       AND NOT EXISTS (SELECT 1 FROM autoevaluacion a
                       WHERE a.id_tema = OLD.id_tema AND a.baja = false AND a.oculto = false
                         AND a.id_autoevaluacion <> OLD.id_autoevaluacion) THEN
        SELECT titulo INTO v_titulo FROM tema WHERE id_tema = OLD.id_tema;
        RAISE EXCEPTION '%', fn_msg_baja('la autoevaluación', OLD.nombre,
                fn_msg_diseno_incompleto('es la única autoevaluación visible del tema «' || v_titulo || '»'),
                'Registrá otra antes de darla de baja.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_baja ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_baja BEFORE UPDATE OF baja ON autoevaluacion
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_autoevaluacion() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_autoevaluacion_pertenencia ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_pertenencia BEFORE UPDATE ON autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_autoevaluacion_sin_borrado ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_sin_borrado BEFORE DELETE ON autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
