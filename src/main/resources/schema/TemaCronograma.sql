-- TemaCronograma: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_tema_cronograma_tema_activo ON tema_cronograma (id_tema) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_tema_cronograma() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.duracion_dias <= 0 THEN
            v_mensaje := fn_msg_formato('La duración', 'una duración entera en días para cada tema del programa');
        ELSIF NEW.numero_orden <= 0 THEN
            v_mensaje := fn_msg_formato('El orden', 'número entero mayor a cero');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_cronograma_validar ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_validar BEFORE INSERT OR UPDATE ON tema_cronograma
    FOR EACH ROW EXECUTE FUNCTION fn_validar_tema_cronograma() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_tema_cronograma_programa_activo ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_programa_activo BEFORE INSERT OR UPDATE OF id_programa, baja ON tema_cronograma
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_programa', 'programa', 'id_programa') ^^^

DROP TRIGGER IF EXISTS trg_tema_cronograma_tema_activa ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON tema_cronograma
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_tema_cronograma_congelado(): disparador del cronograma de un programa.
CREATE OR REPLACE FUNCTION fn_tema_cronograma_congelado() RETURNS trigger AS $$
DECLARE
    v_programa int;
    v_nombre text;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_programa IS NOT DISTINCT FROM NEW.id_programa
       AND OLD.id_tema IS NOT DISTINCT FROM NEW.id_tema
       AND OLD.numero_orden IS NOT DISTINCT FROM NEW.numero_orden
       AND OLD.duracion_dias IS NOT DISTINCT FROM NEW.duracion_dias
       AND OLD.baja IS NOT DISTINCT FROM NEW.baja THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'DELETE' THEN v_programa := OLD.id_programa; ELSE v_programa := NEW.id_programa; END IF;
    IF fn_programa_esta_congelado(v_programa) THEN
        SELECT nombre INTO v_nombre FROM programa WHERE id_programa = v_programa;
        RAISE EXCEPTION '%', fn_msg_publicacion('el cronograma del programa', v_nombre, fn_txt_motivo_publicado(), fn_txt_cierre_diseno())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_cronograma_congelado ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_congelado BEFORE INSERT OR UPDATE OR DELETE ON tema_cronograma
    FOR EACH ROW EXECUTE FUNCTION fn_tema_cronograma_congelado() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_cronograma_completo ON tema_cronograma ^^^
CREATE CONSTRAINT TRIGGER trg_cronograma_completo AFTER INSERT OR UPDATE OR DELETE ON tema_cronograma
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_cronograma_completo() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_tema_cronograma_pertenencia ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_pertenencia BEFORE UPDATE ON tema_cronograma
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_programa', 'id_tema') ^^^
