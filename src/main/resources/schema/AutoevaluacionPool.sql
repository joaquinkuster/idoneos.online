-- AutoevaluacionPool: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_autoevaluacion_pool_pool ON autoevaluacion_pool (id_pool) ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_autoevaluacion_pool_congelado(): disparador de los pools de una autoevaluacion. Rechaza agregar, cambiar o quitar
-- pools cuando la autoevaluacion tiene intentos activos (es cambiar su contenido).
CREATE OR REPLACE FUNCTION fn_autoevaluacion_pool_congelado() RETURNS trigger AS $$
DECLARE
    v_autoevaluacion int;
    v_nombre text;
BEGIN
    IF TG_OP = 'DELETE' THEN v_autoevaluacion := OLD.id_autoevaluacion; ELSE v_autoevaluacion := NEW.id_autoevaluacion; END IF;
    IF fn_autoevaluacion_con_intentos(v_autoevaluacion) THEN
        SELECT nombre INTO v_nombre FROM autoevaluacion WHERE id_autoevaluacion = v_autoevaluacion;
        RAISE EXCEPTION 'Error! La autoevaluación «%» ya registra intentos: solo pueden modificarse la fecha de cierre, la visibilidad y la cantidad de intentos permitidos.', v_nombre
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_pool_congelado ON autoevaluacion_pool ^^^
CREATE TRIGGER trg_autoevaluacion_pool_congelado BEFORE INSERT OR UPDATE OR DELETE ON autoevaluacion_pool
    FOR EACH ROW EXECUTE FUNCTION fn_autoevaluacion_pool_congelado() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_autoevaluacion_pool_pertenencia ON autoevaluacion_pool ^^^
CREATE TRIGGER trg_autoevaluacion_pool_pertenencia BEFORE UPDATE ON autoevaluacion_pool
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_autoevaluacion', 'id_pool') ^^^
