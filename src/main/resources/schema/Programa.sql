-- Programa: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_programa_curso_activo ON programa (id_curso) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_programa() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.nombre) = '' THEN
            v_mensaje := fn_msg_vacio('El nombre del programa');
        ELSIF NEW.descripcion IS NOT NULL AND btrim(NEW.descripcion) = '' THEN
            v_mensaje := fn_msg_vacio('La descripción');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_programa_validar ON programa ^^^
CREATE TRIGGER trg_programa_validar BEFORE INSERT OR UPDATE ON programa
    FOR EACH ROW EXECUTE FUNCTION fn_validar_programa() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_programa_curso_activo ON programa ^^^
CREATE TRIGGER trg_programa_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON programa
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_programa_congelado(): disparador de modificacion de un programa.
CREATE OR REPLACE FUNCTION fn_programa_congelado() RETURNS trigger AS $$
BEGIN
    IF fn_programa_esta_congelado(OLD.id_programa) THEN
        RAISE EXCEPTION '%', fn_msg_publicacion('el programa', OLD.nombre, fn_txt_motivo_publicado(), fn_txt_cierre_diseno())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_programa_congelado ON programa ^^^
CREATE TRIGGER trg_programa_congelado BEFORE UPDATE ON programa
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion
                    OR OLD.id_curso IS DISTINCT FROM NEW.id_curso)
    EXECUTE FUNCTION fn_programa_congelado() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_programa_cronograma_completo ON programa ^^^
CREATE CONSTRAINT TRIGGER trg_programa_cronograma_completo AFTER INSERT ON programa
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_cronograma_completo() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_programa(): un programa no se da de baja si tiene cohortes activas.
CREATE OR REPLACE FUNCTION fn_baja_programa() RETURNS trigger AS $$
DECLARE
    v_cohortes bigint;
BEGIN
    SELECT count(*) INTO v_cohortes FROM cohorte WHERE id_programa = OLD.id_programa AND baja = false;
    IF v_cohortes > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('el programa', OLD.nombre,
                'tiene ' || fn_msg_cantidad(v_cohortes, 'cohorte activa asociada', 'cohortes activas asociadas'),
                'Dé de baja primero ' || CASE WHEN v_cohortes = 1 THEN 'esa cohorte.' ELSE 'esas cohortes.' END)
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_programa_baja ON programa ^^^
CREATE TRIGGER trg_programa_baja BEFORE UPDATE OF baja ON programa
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_programa() ^^^

-- ----------------------------------------------------------------------
-- Cascadas de baja
-- ----------------------------------------------------------------------

-- fn_cascada_programa(): al dar de baja un programa se dan de baja las filas de su cronograma (no puede haber un registro activo dentro de uno dado
-- de baja).
CREATE OR REPLACE FUNCTION fn_cascada_programa() RETURNS trigger AS $$
BEGIN
    UPDATE tema_cronograma SET baja = true, ultima_modificacion = now() WHERE id_programa = NEW.id_programa AND baja = false;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_programa_cascada ON programa ^^^
CREATE TRIGGER trg_programa_cascada AFTER UPDATE OF baja ON programa
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_cascada_programa() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_programa_pertenencia ON programa ^^^
CREATE TRIGGER trg_programa_pertenencia BEFORE UPDATE ON programa
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_curso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_programa_sin_borrado ON programa ^^^
CREATE TRIGGER trg_programa_sin_borrado BEFORE DELETE ON programa
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
