-- Tema: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_tema() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.titulo) = '' THEN
            v_mensaje := fn_msg_vacio('El título del tema');
        ELSIF NEW.descripcion IS NOT NULL AND btrim(NEW.descripcion) = '' THEN
            v_mensaje := fn_msg_vacio('La descripción');
        ELSIF NEW.contenido IS NOT NULL AND btrim(NEW.contenido) = '' THEN
            v_mensaje := fn_msg_vacio('El contenido');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_validar ON tema ^^^
CREATE TRIGGER trg_tema_validar BEFORE INSERT OR UPDATE ON tema
    FOR EACH ROW EXECUTE FUNCTION fn_validar_tema() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_tema_curso_activo ON tema ^^^
CREATE TRIGGER trg_tema_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON tema
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_tema_congelada(): disparador de modificacion de un tema. Se llama solo si cambio el titulo, la descripcion, el contenido o el
-- curso, y rechaza el cambio si el curso del tema tiene cohortes publicadas.
CREATE OR REPLACE FUNCTION fn_tema_congelada() RETURNS trigger AS $$
BEGIN
    IF fn_curso_tiene_cohortes_publicadas(OLD.id_curso) THEN
        RAISE EXCEPTION '%', fn_msg_publicacion('el tema', OLD.titulo, fn_txt_motivo_curso_publicado(), fn_txt_cierre_diseno())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_congelado ON tema ^^^
CREATE TRIGGER trg_tema_congelado BEFORE UPDATE ON tema
    FOR EACH ROW WHEN (OLD.titulo IS DISTINCT FROM NEW.titulo
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion
                    OR OLD.contenido IS DISTINCT FROM NEW.contenido
                    OR OLD.id_curso IS DISTINCT FROM NEW.id_curso)
    EXECUTE FUNCTION fn_tema_congelada() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_tema(): disparador de baja de un tema.
CREATE OR REPLACE FUNCTION fn_baja_tema() RETURNS trigger AS $$
DECLARE
    v_pools bigint;
    v_autoevaluaciones bigint;
    v_dias int;
BEGIN
    SELECT count(*) INTO v_pools FROM pool WHERE id_tema = OLD.id_tema AND baja = false;
    SELECT count(*) INTO v_autoevaluaciones FROM autoevaluacion WHERE id_tema = OLD.id_tema AND baja = false;
    IF v_pools + v_autoevaluaciones > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('el tema', OLD.titulo,
                'tiene ' || CASE WHEN v_pools > 0 THEN fn_msg_cantidad(v_pools, 'pool activo', 'pools activos') ELSE '' END
                         || CASE WHEN v_pools > 0 AND v_autoevaluaciones > 0 THEN ' y ' ELSE '' END
                         || CASE WHEN v_autoevaluaciones > 0 THEN fn_msg_cantidad(v_autoevaluaciones, 'autoevaluación activa', 'autoevaluaciones activas') ELSE '' END,
                'Dé de baja primero ' || CASE WHEN v_pools + v_autoevaluaciones = 1 THEN 'ese registro.' ELSE 'esos registros.' END)
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF fn_curso_tiene_cohortes_publicadas(OLD.id_curso) THEN
        RAISE EXCEPTION '%', fn_msg_baja('el tema', OLD.titulo, fn_txt_motivo_curso_publicado(),
                fn_txt_compromiso_publicado() || ' ' || fn_txt_cierre_diseno())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    SELECT tc.duracion_dias INTO v_dias FROM tema_cronograma tc JOIN programa p ON p.id_programa = tc.id_programa
        WHERE tc.id_tema = OLD.id_tema AND tc.baja = false AND p.baja = false
        ORDER BY tc.id_programa LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION '%', fn_msg_baja('el tema', OLD.titulo,
                fn_msg_diseno_incompleto('dejaría sin asignar ' || fn_msg_duracion(v_dias) || ' del cronograma'),
                'Quitalo antes del programa: modificá el programa o registrá una versión nueva sin este tema.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_baja ON tema ^^^
CREATE TRIGGER trg_tema_baja BEFORE UPDATE OF baja ON tema
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_tema() ^^^

-- ----------------------------------------------------------------------
-- Cascadas de baja
-- ----------------------------------------------------------------------

-- fn_cascada_tema(): al dar de baja un tema se dan de baja sus materiales.
CREATE OR REPLACE FUNCTION fn_cascada_tema() RETURNS trigger AS $$
BEGIN
    UPDATE tema_recurso SET baja = true, ultima_modificacion = now() WHERE id_tema = NEW.id_tema AND baja = false;
    UPDATE termino_glosario SET baja = true, ultima_modificacion = now() WHERE id_tema = NEW.id_tema AND baja = false;
    UPDATE consulta_foro SET baja = true, ultima_modificacion = now() WHERE id_tema = NEW.id_tema AND baja = false;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_cascada ON tema ^^^
CREATE TRIGGER trg_tema_cascada AFTER UPDATE OF baja ON tema
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_cascada_tema() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_tema_pertenencia ON tema ^^^
CREATE TRIGGER trg_tema_pertenencia BEFORE UPDATE ON tema
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_curso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_tema_sin_borrado ON tema ^^^
CREATE TRIGGER trg_tema_sin_borrado BEFORE DELETE ON tema
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
