-- Curso: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_curso_categoria_activo ON curso (id_categoria) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_curso() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.nombre) = '' THEN
            v_mensaje := 'Error! Faltan completar los campos obligatorios: nombre.';
        ELSIF NEW.precio < 0 OR NEW.precio > 10000000 THEN
            v_mensaje := fn_msg_formato('El precio', 'número desde 0 hasta $10.000.000');
        ELSIF NEW.duracion_dias < 1 OR NEW.duracion_dias > 700 THEN
            v_mensaje := fn_msg_formato('La duración', 'mayor a cero y hasta ' || fn_msg_duracion(700));
        ELSIF NEW.carga_horaria_minutos IS NOT NULL AND (NEW.carga_horaria_minutos <= 0 OR NEW.carga_horaria_minutos > 60000) THEN
            v_mensaje := fn_msg_formato('La carga horaria', 'mayor a cero y hasta ' || fn_msg_minutos(60000));
        ELSIF NEW.carga_horaria_minutos IS NOT NULL AND NEW.carga_horaria_minutos > NEW.duracion_dias::bigint * 1440 THEN
            v_mensaje := 'Error! La carga horaria no puede ser mayor al tiempo de la duración del curso.';
        ELSIF NEW.descripcion IS NOT NULL AND btrim(NEW.descripcion) = '' THEN
            v_mensaje := fn_msg_vacio('La descripción');
        ELSIF NEW.objetivos IS NOT NULL AND btrim(NEW.objetivos) = '' THEN
            v_mensaje := fn_msg_vacio('Los objetivos');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_validar ON curso ^^^
CREATE TRIGGER trg_curso_validar BEFORE INSERT OR UPDATE ON curso
    FOR EACH ROW EXECUTE FUNCTION fn_validar_curso() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_curso_categoria_activa ON curso ^^^
CREATE TRIGGER trg_curso_categoria_activa BEFORE INSERT OR UPDATE OF id_categoria, baja ON curso
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_categoria', 'categoria', 'id_categoria') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_curso_congelado(): disparador de modificacion de un curso.
CREATE OR REPLACE FUNCTION fn_curso_congelado() RETURNS trigger AS $$
BEGIN
    IF fn_curso_tiene_cohortes_publicadas(OLD.id_curso) THEN
        RAISE EXCEPTION '%', fn_msg_publicacion('el curso', OLD.nombre, fn_txt_motivo_publicado(), fn_txt_cierre_curso())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_congelado ON curso ^^^
CREATE TRIGGER trg_curso_congelado BEFORE UPDATE ON curso
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion
                    OR OLD.objetivos IS DISTINCT FROM NEW.objetivos
                    OR OLD.id_categoria IS DISTINCT FROM NEW.id_categoria
                    OR OLD.id_nivel IS DISTINCT FROM NEW.id_nivel
                    OR OLD.carga_horaria_minutos IS DISTINCT FROM NEW.carga_horaria_minutos
                    OR OLD.duracion_dias IS DISTINCT FROM NEW.duracion_dias
                    OR OLD.emite_certificado IS DISTINCT FROM NEW.emite_certificado)
    EXECUTE FUNCTION fn_curso_congelado() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_curso_cronograma_completo ON curso ^^^
CREATE CONSTRAINT TRIGGER trg_curso_cronograma_completo AFTER UPDATE OF duracion_dias ON curso
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_cronograma_completo() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_curso(): un curso no se da de baja si tiene programas, temas o cohortes activos. La cohorte cuenta en cualquier
-- estado, mientras no este dada de baja.
CREATE OR REPLACE FUNCTION fn_baja_curso() RETURNS trigger AS $$
DECLARE
    v_programas bigint;
    v_temas bigint;
    v_cohortes bigint;
    v_partes text[] := ARRAY[]::text[];
BEGIN
    SELECT count(*) INTO v_programas FROM programa WHERE id_curso = OLD.id_curso AND baja = false;
    SELECT count(*) INTO v_temas FROM tema WHERE id_curso = OLD.id_curso AND baja = false;
    SELECT count(*) INTO v_cohortes FROM cohorte WHERE id_curso = OLD.id_curso AND baja = false;
    IF v_programas > 0 THEN v_partes := v_partes || fn_msg_cantidad(v_programas, 'programa activo', 'programas activos'); END IF;
    IF v_temas > 0 THEN v_partes := v_partes || fn_msg_cantidad(v_temas, 'tema activo', 'temas activos'); END IF;
    IF v_cohortes > 0 THEN v_partes := v_partes || fn_msg_cantidad(v_cohortes, 'cohorte activa', 'cohortes activas'); END IF;
    IF cardinality(v_partes) > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('el curso', OLD.nombre,
                'tiene ' || CASE WHEN cardinality(v_partes) = 1 THEN v_partes[1]
                                 ELSE array_to_string(v_partes[1:cardinality(v_partes) - 1], ', ') || ' y ' || v_partes[cardinality(v_partes)] END,
                'Dé de baja primero sus programas, temas y cohortes.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_baja ON curso ^^^
CREATE TRIGGER trg_curso_baja BEFORE UPDATE OF baja ON curso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_curso() ^^^

-- ----------------------------------------------------------------------
-- Cascadas de baja
-- ----------------------------------------------------------------------

-- fn_cascada_curso(): al dar de baja un curso se da de baja su equipo docente.
CREATE OR REPLACE FUNCTION fn_cascada_curso() RETURNS trigger AS $$
BEGIN
    UPDATE docente_curso SET baja = true, ultima_modificacion = now() WHERE id_curso = NEW.id_curso AND baja = false;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_cascada ON curso ^^^
CREATE TRIGGER trg_curso_cascada AFTER UPDATE OF baja ON curso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_cascada_curso() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_curso_sin_borrado ON curso ^^^
CREATE TRIGGER trg_curso_sin_borrado BEFORE DELETE ON curso
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
