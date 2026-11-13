-- ClaseEnVivo: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_clase_en_vivo_cohorte_activo ON clase_en_vivo (id_cohorte) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_clase_en_vivo() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.titulo) = '' THEN
            v_mensaje := fn_msg_vacio('El título de la clase');
        ELSIF NEW.fecha_hora_cierre <= NEW.fecha_hora_inicio THEN
            v_mensaje := 'Error! La fecha y hora de cierre deben ser posteriores a las de inicio.';
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_clase_en_vivo_validar ON clase_en_vivo ^^^
CREATE TRIGGER trg_clase_en_vivo_validar BEFORE INSERT OR UPDATE ON clase_en_vivo
    FOR EACH ROW EXECUTE FUNCTION fn_validar_clase_en_vivo() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_clase_en_vivo_cohorte_activa ON clase_en_vivo ^^^
CREATE TRIGGER trg_clase_en_vivo_cohorte_activa BEFORE INSERT OR UPDATE OF id_cohorte, baja ON clase_en_vivo
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_cohorte', 'cohorte', 'id_cohorte') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_clase_en_vivo_sin_borrado ON clase_en_vivo ^^^
CREATE TRIGGER trg_clase_en_vivo_sin_borrado BEFORE DELETE ON clase_en_vivo
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
