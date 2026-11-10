-- Cohorte: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Índices
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_cohorte_programa_activo ON cohorte (id_programa) WHERE baja = false ^^^

CREATE INDEX IF NOT EXISTS ix_cohorte_curso_activo ON cohorte (id_curso) WHERE baja = false ^^^

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_cohorte() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.fecha_fin_inscripcion::date <= NEW.fecha_inicio_inscripcion::date THEN
            v_mensaje := 'Error! La fecha de fin de inscripción debe ser posterior a la fecha de inicio.';
        ELSIF NEW.fecha_inicio_dictado IS NOT NULL AND NEW.fecha_inicio_dictado::date <= NEW.fecha_fin_inscripcion::date THEN
            v_mensaje := 'Error! La fecha de inicio de dictado debe ser posterior a la fecha de fin de inscripción.';
        ELSIF NEW.fecha_inicio_dictado IS NOT NULL AND NEW.fecha_fin_dictado IS NOT NULL AND NEW.fecha_fin_dictado <= NEW.fecha_inicio_dictado THEN
            v_mensaje := 'Error! La fecha de fin de dictado debe ser posterior a la fecha de inicio.';
        ELSIF NEW.cupo_maximo IS NOT NULL AND (NEW.cupo_maximo <= 0 OR NEW.cupo_maximo > 10000) THEN
            v_mensaje := fn_msg_formato('El cupo máximo', 'número entero de 1 a 10000');
        ELSIF NEW.acceso_dias <= 0 OR NEW.acceso_dias > 700 THEN
            v_mensaje := fn_msg_formato('Los días de acceso', 'número entero de 1 a 700');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_validar ON cohorte ^^^
CREATE TRIGGER trg_cohorte_validar BEFORE INSERT OR UPDATE ON cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_validar_cohorte() ^^^

-- ----------------------------------------------------------------------
-- Integridad
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_cohorte_curso_activo ON cohorte ^^^
CREATE TRIGGER trg_cohorte_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON cohorte
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

-- fn_cohorte_coherente(): coherencia de una cohorte.
CREATE OR REPLACE FUNCTION fn_cohorte_coherente() RETURNS trigger AS $$
DECLARE
    v_curso_del_programa int;
    v_estado text;
BEGIN
    IF NEW.id_programa IS NOT NULL THEN
        SELECT id_curso INTO v_curso_del_programa FROM programa WHERE id_programa = NEW.id_programa;
        IF v_curso_del_programa IS DISTINCT FROM NEW.id_curso THEN
            RAISE EXCEPTION 'El programa de la cohorte debe ser del mismo curso que la cohorte'
                USING ERRCODE = 'integrity_constraint_violation', CONSTRAINT = TG_NAME;
        END IF;
    END IF;
    SELECT nombre INTO v_estado FROM estado_cohorte WHERE id_estado_cohorte = NEW.id_estado_cohorte;
    IF v_estado NOT IN ('Pendiente de contenido', 'Cancelada por falta de contenido') AND NEW.id_programa IS NULL THEN
        RAISE EXCEPTION 'Una cohorte publicada necesita un programa'
            USING ERRCODE = 'integrity_constraint_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_coherente ON cohorte ^^^
CREATE TRIGGER trg_cohorte_coherente BEFORE INSERT OR UPDATE OF id_curso, id_programa, id_estado_cohorte ON cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_cohorte_coherente() ^^^

-- fn_cohorte_acceso(): los dias de acceso de una cohorte incluyen toda la duracion del curso. Lo que se agrega es el tiempo
-- extra para quien se atrase.
CREATE OR REPLACE FUNCTION fn_cohorte_acceso() RETURNS trigger AS $$
DECLARE
    v_duracion int;
BEGIN
    SELECT duracion_dias INTO v_duracion FROM curso WHERE id_curso = NEW.id_curso;
    IF NEW.acceso_dias < v_duracion THEN
        RAISE EXCEPTION 'Error! Los días de acceso (%) no pueden ser menores a la duración del curso (% días).', NEW.acceso_dias, v_duracion
            USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_acceso ON cohorte ^^^
CREATE TRIGGER trg_cohorte_acceso BEFORE INSERT OR UPDATE OF id_curso, acceso_dias ON cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_cohorte_acceso() ^^^

DROP TRIGGER IF EXISTS trg_cohorte_programa_activo ON cohorte ^^^
CREATE TRIGGER trg_cohorte_programa_activo BEFORE INSERT OR UPDATE OF id_programa, baja ON cohorte
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_programa', 'programa', 'id_programa') ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_cohorte_congelada(): disparador de modificacion de una cohorte. Se llama solo si cambio alguna fecha, el cupo, los dias de
-- acceso o el programa, y rechaza el cambio si la cohorte esta publicada.
CREATE OR REPLACE FUNCTION fn_cohorte_congelada() RETURNS trigger AS $$
BEGIN
    IF fn_cohorte_esta_publicada(OLD.id_cohorte) THEN
        RAISE EXCEPTION '%', fn_msg_publicacion('la cohorte', fn_cohorte_etiqueta(OLD.id_cohorte), 'está publicada en el catálogo', fn_txt_cierre_cohorte())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_congelado ON cohorte ^^^
CREATE TRIGGER trg_cohorte_congelado BEFORE UPDATE ON cohorte
    FOR EACH ROW WHEN (OLD.fecha_inicio_inscripcion IS DISTINCT FROM NEW.fecha_inicio_inscripcion
                    OR OLD.fecha_fin_inscripcion IS DISTINCT FROM NEW.fecha_fin_inscripcion
                    OR OLD.fecha_inicio_dictado IS DISTINCT FROM NEW.fecha_inicio_dictado
                    OR OLD.fecha_fin_dictado IS DISTINCT FROM NEW.fecha_fin_dictado
                    OR OLD.cupo_maximo IS DISTINCT FROM NEW.cupo_maximo
                    OR OLD.acceso_dias IS DISTINCT FROM NEW.acceso_dias
                    OR OLD.id_programa IS DISTINCT FROM NEW.id_programa)
    EXECUTE FUNCTION fn_cohorte_congelada() ^^^

-- fn_cohorte_asignar_programa(): disparador de la asignacion de un programa a una cohorte (CohorteService.asignarPrograma).
CREATE OR REPLACE FUNCTION fn_cohorte_asignar_programa() RETURNS trigger AS $$
DECLARE
    v_estado text;
    v_inscriptos bigint;
BEGIN
    SELECT e.nombre INTO v_estado FROM estado_cohorte e WHERE e.id_estado_cohorte = OLD.id_estado_cohorte;
    IF v_estado <> 'Pendiente de contenido' THEN
        RAISE EXCEPTION '%', fn_msg_accion('asignar el programa a', 'la cohorte', fn_cohorte_etiqueta(OLD.id_cohorte),
                CASE WHEN v_estado = 'Cancelada por falta de contenido' THEN 'está cancelada'
                     ELSE 'ya tiene su diseño finalizado y está publicada en el catálogo' END,
                fn_txt_compromiso_publicado() || ' ' || fn_txt_cierre_cohorte())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    SELECT count(*) INTO v_inscriptos FROM inscripcion WHERE id_cohorte = OLD.id_cohorte AND baja = false;
    IF v_inscriptos > 0 THEN
        RAISE EXCEPTION '%', fn_msg_accion('asignar el programa a', 'la cohorte', fn_cohorte_etiqueta(OLD.id_cohorte),
                'tiene ' || fn_msg_cantidad(v_inscriptos, 'inscripción activa asociada', 'inscripciones activas asociadas'), '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_asignar_programa ON cohorte ^^^
CREATE TRIGGER trg_cohorte_asignar_programa BEFORE UPDATE OF id_programa ON cohorte
    FOR EACH ROW WHEN (OLD.id_programa IS DISTINCT FROM NEW.id_programa)
    EXECUTE FUNCTION fn_cohorte_asignar_programa() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- Una cohorte nunca tiene un programa incompleto (CohorteService.asignarPrograma): al asignarle el programa, o al publicarla con el
-- que tiene, el programa tiene que estar completo.

-- fn_cohorte_programa_completo(): disparador de la publicacion de una cohorte.
CREATE OR REPLACE FUNCTION fn_cohorte_programa_completo() RETURNS trigger AS $$
DECLARE
    v_estado text;
    v_estado_anterior text;
    v_faltantes text[];
    v_programa text;
BEGIN
    SELECT nombre INTO v_estado FROM estado_cohorte WHERE id_estado_cohorte = NEW.id_estado_cohorte;
    IF NEW.baja = true OR NEW.id_programa IS NULL OR v_estado IN ('Pendiente de contenido', 'Cancelada por falta de contenido') THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'UPDATE' THEN
        SELECT nombre INTO v_estado_anterior FROM estado_cohorte WHERE id_estado_cohorte = OLD.id_estado_cohorte;
        IF OLD.id_programa IS NOT DISTINCT FROM NEW.id_programa
           AND v_estado_anterior NOT IN ('Pendiente de contenido', 'Cancelada por falta de contenido') THEN
            RETURN NEW;
        END IF;
    END IF;
    v_faltantes := fn_programa_faltantes(NEW.id_programa);
    IF cardinality(v_faltantes) > 0 THEN
        SELECT nombre INTO v_programa FROM programa WHERE id_programa = NEW.id_programa;
        RAISE EXCEPTION '%', fn_msg_accion('asignar', 'el programa', v_programa,
                'todavía no está completo (' || array_to_string(v_faltantes, '; ') || ')', 'Completá su cronograma y su contenido antes.')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_programa_completo ON cohorte ^^^
CREATE TRIGGER trg_cohorte_programa_completo BEFORE INSERT OR UPDATE OF id_programa, id_estado_cohorte, baja ON cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_cohorte_programa_completo() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- fn_baja_cohorte(): una cohorte no se da de baja si tiene inscripciones o clases en vivo activas.
CREATE OR REPLACE FUNCTION fn_baja_cohorte() RETURNS trigger AS $$
DECLARE
    v_inscriptos bigint;
    v_clases bigint;
    v_nombre text;
BEGIN
    SELECT count(*) INTO v_inscriptos FROM inscripcion WHERE id_cohorte = OLD.id_cohorte AND baja = false;
    SELECT count(*) INTO v_clases FROM clase_en_vivo WHERE id_cohorte = OLD.id_cohorte AND baja = false;
    IF v_inscriptos > 0 OR v_clases > 0 THEN
        SELECT cu.nombre || CASE WHEN p.id_programa IS NOT NULL THEN ' - ' || p.nombre ELSE '' END INTO v_nombre
            FROM curso cu LEFT JOIN programa p ON p.id_programa = OLD.id_programa WHERE cu.id_curso = OLD.id_curso;
        RAISE EXCEPTION '%', fn_msg_baja('la cohorte', v_nombre,
                'tiene ' || CASE WHEN v_inscriptos > 0 THEN fn_msg_cantidad(v_inscriptos, 'inscripción activa asociada', 'inscripciones activas asociadas') ELSE '' END
                         || CASE WHEN v_inscriptos > 0 AND v_clases > 0 THEN ' y ' ELSE '' END
                         || CASE WHEN v_clases > 0 THEN fn_msg_cantidad(v_clases, 'clase en vivo activa', 'clases en vivo activas') ELSE '' END,
                CASE WHEN v_inscriptos = 0 THEN 'Dé de baja primero sus clases en vivo.' ELSE '' END)
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_baja ON cohorte ^^^
CREATE TRIGGER trg_cohorte_baja BEFORE UPDATE OF baja ON cohorte
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_cohorte() ^^^

-- ----------------------------------------------------------------------
-- Cascadas de baja
-- ----------------------------------------------------------------------

-- fn_cascada_cohorte(): al dar de baja una cohorte se desvincula a los docentes que la tenian como su cohorte de trabajo.
CREATE OR REPLACE FUNCTION fn_cascada_cohorte() RETURNS trigger AS $$
BEGIN
    UPDATE docente_curso SET id_cohorte = NULL, ultima_modificacion = now() WHERE id_cohorte = NEW.id_cohorte;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_cohorte_cascada ON cohorte ^^^
CREATE TRIGGER trg_cohorte_cascada AFTER UPDATE OF baja ON cohorte
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_cascada_cohorte() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_cohorte_pertenencia ON cohorte ^^^
CREATE TRIGGER trg_cohorte_pertenencia BEFORE UPDATE ON cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_curso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_cohorte_sin_borrado ON cohorte ^^^
CREATE TRIGGER trg_cohorte_sin_borrado BEFORE DELETE ON cohorte
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
