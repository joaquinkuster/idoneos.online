-- Indices y disparadores de la base de datos (solo PostgreSQL). Spring lo ejecuta despues de que Hibernate crea las tablas.
-- Cada sentencia termina con ^^^ (en lugar de punto y coma) porque los disparadores tienen punto y coma adentro.
-- Se puede ejecutar mas de una vez: usa IF NOT EXISTS y DROP ... IF EXISTS.

-- ---------------------------------------------------------------------------------------------
-- Indices unicos parciales: la unicidad vale solo entre los registros activos (baja = false)
-- ---------------------------------------------------------------------------------------------

-- CU-45 y CU-46: un alumno no puede tener dos inscripciones vigentes a la misma cohorte
CREATE UNIQUE INDEX IF NOT EXISTS uq_inscripcion_alumno_cohorte_activo
    ON inscripcion (id_alumno, id_cohorte) WHERE baja = false ^^^

-- CU-03 y CU-04: un docente figura una sola vez por curso (el titular no puede ser tambien ayudante)
CREATE UNIQUE INDEX IF NOT EXISTS uq_docente_curso_curso_docente_activo
    ON docente_curso (id_curso, id_docente) WHERE baja = false ^^^

-- ---------------------------------------------------------------------------------------------
-- Indices de apoyo: aceleran las consultas de dependientes activos que hacen las bajas y los
-- datos congelados. PostgreSQL no crea indices sobre las claves foraneas por si solo.
-- (Unidad por curso, Pool por unidad y TerminoGlosario por unidad ya los cubre un UNIQUE existente.)
-- ---------------------------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_curso_categoria_activo ON curso (id_categoria) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_programa_curso_activo ON programa (id_curso) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_cohorte_programa_activo ON cohorte (id_programa) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_inscripcion_cohorte_activo ON inscripcion (id_cohorte) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_clase_en_vivo_cohorte_activo ON clase_en_vivo (id_cohorte) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_material_unidad_activo ON material (id_unidad) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_autoevaluacion_unidad_activo ON autoevaluacion (id_unidad) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_intento_autoevaluacion_autoevaluacion_activo ON intento_autoevaluacion (id_autoevaluacion) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_consulta_foro_unidad_activo ON consulta_foro (id_unidad) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_respuesta_foro_consulta_activo ON respuesta_foro (id_consulta) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_unidad_cronograma_unidad_activo ON unidad_cronograma (id_unidad) WHERE baja = false ^^^
-- AutoevaluacionPool no tiene baja: se consulta por pool para saber si esta en alguna autoevaluacion
CREATE INDEX IF NOT EXISTS ix_autoevaluacion_pool_pool ON autoevaluacion_pool (id_pool) ^^^

-- ---------------------------------------------------------------------------------------------
-- Disparadores de baja: no se da de baja un registro que tiene registros activos que dependen de el.
-- Se activan solo cuando baja pasa de false a true. Si hay dependientes, la base rechaza el cambio.
-- ---------------------------------------------------------------------------------------------

-- Funcion generica. Argumentos del disparador: primero la columna clave del registro y luego cada
-- dependiente con el formato tabla:columna_que_apunta_al_registro
CREATE OR REPLACE FUNCTION fn_baja_sin_dependientes() RETURNS trigger AS $$
DECLARE
    id_registro text := to_jsonb(OLD) ->> TG_ARGV[0];
    tabla text;
    columna text;
    cantidad bigint;
    i int;
BEGIN
    FOR i IN 1 .. TG_NARGS - 1 LOOP
        tabla := split_part(TG_ARGV[i], ':', 1);
        columna := split_part(TG_ARGV[i], ':', 2);
        EXECUTE format('SELECT count(*) FROM %I WHERE %I = $1::int AND baja = false', tabla, columna)
            INTO cantidad USING id_registro;
        IF cantidad > 0 THEN
            RAISE EXCEPTION 'No se puede dar de baja: tiene % registro(s) activo(s) en %', cantidad, tabla
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
    END LOOP;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

-- CU-05: un curso no se da de baja si tiene programas o unidades activas
DROP TRIGGER IF EXISTS trg_curso_baja ON curso ^^^
CREATE TRIGGER trg_curso_baja BEFORE UPDATE OF baja ON curso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_curso', 'programa:id_curso', 'unidad:id_curso') ^^^

-- CU-10: una categoria no se da de baja si tiene cursos activos
DROP TRIGGER IF EXISTS trg_categoria_baja ON categoria ^^^
CREATE TRIGGER trg_categoria_baja BEFORE UPDATE OF baja ON categoria
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_categoria', 'curso:id_categoria') ^^^

-- CU-14: una cohorte no se da de baja si tiene inscripciones o clases en vivo activas
DROP TRIGGER IF EXISTS trg_cohorte_baja ON cohorte ^^^
CREATE TRIGGER trg_cohorte_baja BEFORE UPDATE OF baja ON cohorte
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_cohorte', 'inscripcion:id_cohorte', 'clase_en_vivo:id_cohorte') ^^^

-- CU-18: un programa no se da de baja si tiene cohortes activas
DROP TRIGGER IF EXISTS trg_programa_baja ON programa ^^^
CREATE TRIGGER trg_programa_baja BEFORE UPDATE OF baja ON programa
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_programa', 'cohorte:id_programa') ^^^

-- CU-67: una autoevaluacion no se da de baja si tiene intentos activos
DROP TRIGGER IF EXISTS trg_autoevaluacion_baja ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_baja BEFORE UPDATE OF baja ON autoevaluacion
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_autoevaluacion', 'intento_autoevaluacion:id_autoevaluacion') ^^^

-- CU-22: una unidad no se da de baja si esta en el cronograma de un programa activo, si tiene pools o
-- autoevaluaciones activos, o si el curso al que pertenece tiene inscripciones activas
CREATE OR REPLACE FUNCTION fn_baja_unidad() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM unidad_cronograma uc
               JOIN programa p ON p.id_programa = uc.id_programa
               WHERE uc.id_unidad = OLD.id_unidad AND uc.baja = false AND p.baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja la unidad: pertenece al cronograma de un programa activo'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF EXISTS (SELECT 1 FROM pool WHERE id_unidad = OLD.id_unidad AND baja = false)
       OR EXISTS (SELECT 1 FROM autoevaluacion WHERE id_unidad = OLD.id_unidad AND baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja la unidad: tiene pools o autoevaluaciones activos'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF EXISTS (SELECT 1 FROM inscripcion i
               JOIN cohorte c ON c.id_cohorte = i.id_cohorte
               JOIN programa p ON p.id_programa = c.id_programa
               WHERE p.id_curso = OLD.id_curso AND i.baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja la unidad: el curso tiene inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_unidad_baja ON unidad ^^^
CREATE TRIGGER trg_unidad_baja BEFORE UPDATE OF baja ON unidad
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_unidad() ^^^

-- CU-59: un descuento no se da de baja si ya fue usado
CREATE OR REPLACE FUNCTION fn_baja_descuento() RETURNS trigger AS $$
BEGIN
    IF OLD.cantidad_usada > 0 THEN
        RAISE EXCEPTION 'No se puede dar de baja el descuento: ya fue usado % vez/veces', OLD.cantidad_usada
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_descuento_baja ON descuento ^^^
CREATE TRIGGER trg_descuento_baja BEFORE UPDATE OF baja ON descuento
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_descuento() ^^^

-- CU-63: un pool no se da de baja si esta asociado a una autoevaluacion activa
CREATE OR REPLACE FUNCTION fn_baja_pool() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM autoevaluacion_pool ap
               JOIN autoevaluacion a ON a.id_autoevaluacion = ap.id_autoevaluacion
               WHERE ap.id_pool = OLD.id_pool AND a.baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja el pool: esta asociado a una autoevaluacion activa'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pool_baja ON pool ^^^
CREATE TRIGGER trg_pool_baja BEFORE UPDATE OF baja ON pool
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_pool() ^^^

-- CU-04: un docente no se desvincula de un curso si tiene material o clases activas en el
DROP TRIGGER IF EXISTS trg_docente_curso_baja ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_baja BEFORE UPDATE OF baja ON docente_curso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_docente_curso', 'material:id_docente_curso', 'clase_en_vivo:id_docente_curso', 'clase_clon:id_docente_curso') ^^^

-- ---------------------------------------------------------------------------------------------
-- Disparadores de datos congelados: lo que el alumno vio, pago o rindio no se puede modificar.
-- "Activo" significa que no fue dado de baja (baja = false).
-- ---------------------------------------------------------------------------------------------

-- Funciones auxiliares: indican si hay inscripciones activas (o intentos activos) asociados
CREATE OR REPLACE FUNCTION fn_curso_con_inscripciones(p_id_curso int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM inscripcion i
                   JOIN cohorte c ON c.id_cohorte = i.id_cohorte
                   JOIN programa p ON p.id_programa = c.id_programa
                   WHERE p.id_curso = p_id_curso AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

CREATE OR REPLACE FUNCTION fn_categoria_con_inscripciones(p_id_categoria int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM inscripcion i
                   JOIN cohorte c ON c.id_cohorte = i.id_cohorte
                   JOIN programa p ON p.id_programa = c.id_programa
                   JOIN curso cu ON cu.id_curso = p.id_curso
                   WHERE cu.id_categoria = p_id_categoria AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

CREATE OR REPLACE FUNCTION fn_programa_con_inscripciones(p_id_programa int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM inscripcion i
                   JOIN cohorte c ON c.id_cohorte = i.id_cohorte
                   WHERE c.id_programa = p_id_programa AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

CREATE OR REPLACE FUNCTION fn_autoevaluacion_con_intentos(p_id_autoevaluacion int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM intento_autoevaluacion
                   WHERE id_autoevaluacion = p_id_autoevaluacion AND baja = false)
$$ LANGUAGE sql STABLE ^^^

CREATE OR REPLACE FUNCTION fn_pool_con_intentos(p_id_pool int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM intento_autoevaluacion i
                   JOIN autoevaluacion_pool ap ON ap.id_autoevaluacion = i.id_autoevaluacion
                   WHERE ap.id_pool = p_id_pool AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

-- CU-04: con inscripciones activas solo se modifican el precio, el equipo docente y la imagen
CREATE OR REPLACE FUNCTION fn_curso_congelado() RETURNS trigger AS $$
BEGIN
    IF fn_curso_con_inscripciones(OLD.id_curso) THEN
        RAISE EXCEPTION 'No se puede modificar el curso: tiene inscripciones activas (solo se modifican el precio, el equipo docente y la imagen)'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_congelado ON curso ^^^
CREATE TRIGGER trg_curso_congelado BEFORE UPDATE ON curso
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion
                    OR OLD.id_categoria IS DISTINCT FROM NEW.id_categoria
                    OR OLD.id_nivel IS DISTINCT FROM NEW.id_nivel
                    OR OLD.emite_certificado IS DISTINCT FROM NEW.emite_certificado)
    EXECUTE FUNCTION fn_curso_congelado() ^^^

-- CU-04: con inscripciones activas tampoco se agregan ni se quitan modalidades de dictado
CREATE OR REPLACE FUNCTION fn_curso_modalidad_congelada() RETURNS trigger AS $$
DECLARE
    v_curso int;
BEGIN
    IF TG_OP = 'DELETE' THEN v_curso := OLD.id_curso; ELSE v_curso := NEW.id_curso; END IF;
    IF fn_curso_con_inscripciones(v_curso) THEN
        RAISE EXCEPTION 'No se pueden modificar las modalidades: el curso tiene inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_modalidad_congelado ON curso_modalidad ^^^
CREATE TRIGGER trg_curso_modalidad_congelado BEFORE INSERT OR UPDATE OR DELETE ON curso_modalidad
    FOR EACH ROW EXECUTE FUNCTION fn_curso_modalidad_congelada() ^^^

-- CU-09: una categoria con inscripciones activas (a traves de sus cursos) no se modifica
CREATE OR REPLACE FUNCTION fn_categoria_congelada() RETURNS trigger AS $$
BEGIN
    IF fn_categoria_con_inscripciones(OLD.id_categoria) THEN
        RAISE EXCEPTION 'No se puede modificar la categoria: sus cursos tienen inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_categoria_congelado ON categoria ^^^
CREATE TRIGGER trg_categoria_congelado BEFORE UPDATE ON categoria
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre OR OLD.descripcion IS DISTINCT FROM NEW.descripcion)
    EXECUTE FUNCTION fn_categoria_congelada() ^^^

-- CU-13: una cohorte con inscripciones activas no se modifica
CREATE OR REPLACE FUNCTION fn_cohorte_congelada() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM inscripcion WHERE id_cohorte = OLD.id_cohorte AND baja = false) THEN
        RAISE EXCEPTION 'No se puede modificar la cohorte: tiene inscripciones activas'
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
                    OR OLD.semanas_acceso IS DISTINCT FROM NEW.semanas_acceso
                    OR OLD.id_programa IS DISTINCT FROM NEW.id_programa)
    EXECUTE FUNCTION fn_cohorte_congelada() ^^^

-- CU-17: un programa con inscripciones activas no se modifica
CREATE OR REPLACE FUNCTION fn_programa_congelado() RETURNS trigger AS $$
BEGIN
    IF fn_programa_con_inscripciones(OLD.id_programa) THEN
        RAISE EXCEPTION 'No se puede modificar el programa: tiene inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_programa_congelado ON programa ^^^
CREATE TRIGGER trg_programa_congelado BEFORE UPDATE ON programa
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion
                    OR OLD.objetivos IS DISTINCT FROM NEW.objetivos
                    OR OLD.bibliografia IS DISTINCT FROM NEW.bibliografia
                    OR OLD.id_curso IS DISTINCT FROM NEW.id_curso)
    EXECUTE FUNCTION fn_programa_congelado() ^^^

-- CU-21: una unidad cuyo curso tiene inscripciones activas no se modifica
CREATE OR REPLACE FUNCTION fn_unidad_congelada() RETURNS trigger AS $$
BEGIN
    IF fn_curso_con_inscripciones(OLD.id_curso) THEN
        RAISE EXCEPTION 'No se puede modificar la unidad: el curso tiene inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_unidad_congelado ON unidad ^^^
CREATE TRIGGER trg_unidad_congelado BEFORE UPDATE ON unidad
    FOR EACH ROW WHEN (OLD.titulo IS DISTINCT FROM NEW.titulo
                    OR OLD.descripcion IS DISTINCT FROM NEW.descripcion
                    OR OLD.contenido IS DISTINCT FROM NEW.contenido
                    OR OLD.id_curso IS DISTINCT FROM NEW.id_curso)
    EXECUTE FUNCTION fn_unidad_congelada() ^^^

-- CU-25: el cronograma de un programa con inscripciones activas no se modifica
CREATE OR REPLACE FUNCTION fn_unidad_cronograma_congelado() RETURNS trigger AS $$
DECLARE
    v_programa int;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_programa IS NOT DISTINCT FROM NEW.id_programa
       AND OLD.id_unidad IS NOT DISTINCT FROM NEW.id_unidad
       AND OLD.numero_orden IS NOT DISTINCT FROM NEW.numero_orden
       AND OLD.semanas_duracion IS NOT DISTINCT FROM NEW.semanas_duracion
       AND OLD.baja IS NOT DISTINCT FROM NEW.baja THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'DELETE' THEN v_programa := OLD.id_programa; ELSE v_programa := NEW.id_programa; END IF;
    IF fn_programa_con_inscripciones(v_programa) THEN
        RAISE EXCEPTION 'No se puede modificar el cronograma: el programa tiene inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_unidad_cronograma_congelado ON unidad_cronograma ^^^
CREATE TRIGGER trg_unidad_cronograma_congelado BEFORE INSERT OR UPDATE OR DELETE ON unidad_cronograma
    FOR EACH ROW EXECUTE FUNCTION fn_unidad_cronograma_congelado() ^^^

-- CU-62: un pool con intentos activos (en alguna autoevaluacion que lo usa) no cambia su nombre,
-- sus preguntas ni sus opciones
CREATE OR REPLACE FUNCTION fn_pool_congelado() RETURNS trigger AS $$
BEGIN
    IF fn_pool_con_intentos(OLD.id_pool) THEN
        RAISE EXCEPTION 'No se puede modificar el pool: tiene intentos de autoevaluacion activos'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pool_congelado ON pool ^^^
CREATE TRIGGER trg_pool_congelado BEFORE UPDATE ON pool
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre OR OLD.id_unidad IS DISTINCT FROM NEW.id_unidad)
    EXECUTE FUNCTION fn_pool_congelado() ^^^

CREATE OR REPLACE FUNCTION fn_pregunta_congelada() RETURNS trigger AS $$
DECLARE
    v_pool int;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_pool IS NOT DISTINCT FROM NEW.id_pool
       AND OLD.texto IS NOT DISTINCT FROM NEW.texto
       AND OLD.es_opcion_multiple IS NOT DISTINCT FROM NEW.es_opcion_multiple
       AND OLD.baja IS NOT DISTINCT FROM NEW.baja THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'DELETE' THEN v_pool := OLD.id_pool; ELSE v_pool := NEW.id_pool; END IF;
    IF fn_pool_con_intentos(v_pool) THEN
        RAISE EXCEPTION 'No se puede modificar la pregunta: el pool tiene intentos de autoevaluacion activos'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_pregunta_congelado ON pregunta ^^^
CREATE TRIGGER trg_pregunta_congelado BEFORE INSERT OR UPDATE OR DELETE ON pregunta
    FOR EACH ROW EXECUTE FUNCTION fn_pregunta_congelada() ^^^

CREATE OR REPLACE FUNCTION fn_opcion_respuesta_congelada() RETURNS trigger AS $$
DECLARE
    v_pregunta int;
    v_pool int;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_pregunta IS NOT DISTINCT FROM NEW.id_pregunta
       AND OLD.texto IS NOT DISTINCT FROM NEW.texto
       AND OLD.es_correcta IS NOT DISTINCT FROM NEW.es_correcta
       AND OLD.baja IS NOT DISTINCT FROM NEW.baja THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'DELETE' THEN v_pregunta := OLD.id_pregunta; ELSE v_pregunta := NEW.id_pregunta; END IF;
    SELECT id_pool INTO v_pool FROM pregunta WHERE id_pregunta = v_pregunta;
    IF fn_pool_con_intentos(v_pool) THEN
        RAISE EXCEPTION 'No se puede modificar la opcion: el pool tiene intentos de autoevaluacion activos'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_opcion_respuesta_congelado ON opcion_respuesta ^^^
CREATE TRIGGER trg_opcion_respuesta_congelado BEFORE INSERT OR UPDATE OR DELETE ON opcion_respuesta
    FOR EACH ROW EXECUTE FUNCTION fn_opcion_respuesta_congelada() ^^^

-- CU-66: una autoevaluacion con intentos activos protege su contenido. Solo se puede extender la fecha
-- de cierre, ocultar o mostrar, y ampliar la cantidad de intentos permitidos.
CREATE OR REPLACE FUNCTION fn_autoevaluacion_congelada() RETURNS trigger AS $$
DECLARE
    v_contenido boolean;
    v_retroceso boolean;
BEGIN
    v_contenido := OLD.id_unidad IS DISTINCT FROM NEW.id_unidad
                OR OLD.nombre IS DISTINCT FROM NEW.nombre
                OR OLD.tiempo_limite IS DISTINCT FROM NEW.tiempo_limite
                OR OLD.cantidad_preguntas IS DISTINCT FROM NEW.cantidad_preguntas
                OR OLD.nota_minima IS DISTINCT FROM NEW.nota_minima
                OR OLD.fecha_apertura IS DISTINCT FROM NEW.fecha_apertura
                OR OLD.descuento_por_error IS DISTINCT FROM NEW.descuento_por_error
                OR OLD.navegacion_libre IS DISTINCT FROM NEW.navegacion_libre
                OR OLD.permite_salir IS DISTINCT FROM NEW.permite_salir;
    v_retroceso := (OLD.fecha_cierre IS NOT NULL AND NEW.fecha_cierre IS NOT NULL AND NEW.fecha_cierre < OLD.fecha_cierre)
                OR (OLD.fecha_cierre IS NULL AND NEW.fecha_cierre IS NOT NULL)
                OR (OLD.intentos_permitidos IS NOT NULL AND NEW.intentos_permitidos IS NOT NULL AND NEW.intentos_permitidos < OLD.intentos_permitidos)
                OR (OLD.intentos_permitidos IS NULL AND NEW.intentos_permitidos IS NOT NULL);
    IF (v_contenido OR v_retroceso) AND fn_autoevaluacion_con_intentos(OLD.id_autoevaluacion) THEN
        RAISE EXCEPTION 'No se puede modificar la autoevaluacion: tiene intentos activos (solo se puede extender el cierre, ocultarla o ampliar los intentos)'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_congelado ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_congelado BEFORE UPDATE ON autoevaluacion
    FOR EACH ROW EXECUTE FUNCTION fn_autoevaluacion_congelada() ^^^

-- CU-66: los pools de una autoevaluacion con intentos activos tampoco cambian
CREATE OR REPLACE FUNCTION fn_autoevaluacion_pool_congelado() RETURNS trigger AS $$
DECLARE
    v_autoevaluacion int;
BEGIN
    IF TG_OP = 'DELETE' THEN v_autoevaluacion := OLD.id_autoevaluacion; ELSE v_autoevaluacion := NEW.id_autoevaluacion; END IF;
    IF fn_autoevaluacion_con_intentos(v_autoevaluacion) THEN
        RAISE EXCEPTION 'No se pueden modificar los pools: la autoevaluacion tiene intentos activos'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_pool_congelado ON autoevaluacion_pool ^^^
CREATE TRIGGER trg_autoevaluacion_pool_congelado BEFORE INSERT OR UPDATE OR DELETE ON autoevaluacion_pool
    FOR EACH ROW EXECUTE FUNCTION fn_autoevaluacion_pool_congelado() ^^^

-- ---------------------------------------------------------------------------------------------
-- Cupo de la cohorte (CU-45 y CU-46): una inscripcion habilitada ocupa un cupo. Al habilitarse se bloquea la
-- cohorte mientras se cuentan los cupos ocupados, asi dos alumnos no pueden tomar el ultimo lugar a la vez.
-- ---------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_cupo_cohorte() RETURNS trigger AS $$
DECLARE
    v_cupo int;
    v_ocupados bigint;
BEGIN
    IF NEW.habilitado IS NOT TRUE OR NEW.baja = true THEN
        RETURN NEW;
    END IF;
    IF TG_OP = 'UPDATE' AND OLD.habilitado IS TRUE AND OLD.baja = false AND OLD.id_cohorte = NEW.id_cohorte THEN
        RETURN NEW;
    END IF;
    SELECT cupo_maximo INTO v_cupo FROM cohorte WHERE id_cohorte = NEW.id_cohorte FOR UPDATE;
    IF v_cupo IS NULL THEN
        RETURN NEW;
    END IF;
    SELECT count(*) INTO v_ocupados FROM inscripcion
        WHERE id_cohorte = NEW.id_cohorte AND baja = false AND habilitado = true AND id_inscripcion <> NEW.id_inscripcion;
    IF v_ocupados >= v_cupo THEN
        RAISE EXCEPTION 'No se puede habilitar la inscripcion: la cohorte alcanzo su cupo maximo (%)', v_cupo
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

-- ---------------------------------------------------------------------------------------------
-- Columnas que nunca cambian despues del alta (CU-38, 42, 47 y 48). Argumentos: las columnas protegidas.
-- ---------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_columnas_inmutables() RETURNS trigger AS $$
DECLARE
    i int;
BEGIN
    FOR i IN 0 .. TG_NARGS - 1 LOOP
        IF (to_jsonb(OLD) ->> TG_ARGV[i]) IS DISTINCT FROM (to_jsonb(NEW) ->> TG_ARGV[i]) THEN
            RAISE EXCEPTION 'No se puede modificar la columna % de %', TG_ARGV[i], TG_TABLE_NAME
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
    END LOOP;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_inscripcion_inmutable ON inscripcion ^^^
CREATE TRIGGER trg_inscripcion_inmutable BEFORE UPDATE ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_cohorte', 'id_alumno') ^^^

DROP TRIGGER IF EXISTS trg_consulta_foro_inmutable ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_inmutable BEFORE UPDATE ON consulta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_unidad', 'id_inscripcion') ^^^

DROP TRIGGER IF EXISTS trg_respuesta_foro_inmutable ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_inmutable BEFORE UPDATE ON respuesta_foro
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_consulta', 'id_docente_curso') ^^^

DROP TRIGGER IF EXISTS trg_certificado_inmutable ON certificado ^^^
CREATE TRIGGER trg_certificado_inmutable BEFORE UPDATE ON certificado
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('numero', 'id_inscripcion', 'fecha_emision') ^^^

-- ---------------------------------------------------------------------------------------------
-- Coherencia padre-hijo: no se crea ni se reactiva un registro activo dentro de uno dado de baja.
-- Argumentos: columna del hijo que apunta al padre, tabla del padre y columna clave del padre.
-- (Excepcion CU-49: las consultas de foro de una inscripcion dada de baja se conservan, por eso no se controla.)
-- ---------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_padre_activo() RETURNS trigger AS $$
DECLARE
    v_baja boolean;
BEGIN
    EXECUTE format('SELECT baja FROM %I WHERE %I = $1::int', TG_ARGV[1], TG_ARGV[2])
        INTO v_baja USING (to_jsonb(NEW) ->> TG_ARGV[0]);
    IF v_baja IS TRUE THEN
        RAISE EXCEPTION 'No se puede crear ni reactivar el registro: su % esta dado de baja', TG_ARGV[1]
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_curso_categoria_activa ON curso ^^^
CREATE TRIGGER trg_curso_categoria_activa BEFORE INSERT OR UPDATE OF id_categoria, baja ON curso
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_categoria', 'categoria', 'id_categoria') ^^^

DROP TRIGGER IF EXISTS trg_programa_curso_activo ON programa ^^^
CREATE TRIGGER trg_programa_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON programa
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

DROP TRIGGER IF EXISTS trg_unidad_curso_activo ON unidad ^^^
CREATE TRIGGER trg_unidad_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON unidad
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

DROP TRIGGER IF EXISTS trg_docente_curso_curso_activo ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON docente_curso
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

DROP TRIGGER IF EXISTS trg_cohorte_programa_activo ON cohorte ^^^
CREATE TRIGGER trg_cohorte_programa_activo BEFORE INSERT OR UPDATE OF id_programa, baja ON cohorte
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_programa', 'programa', 'id_programa') ^^^

DROP TRIGGER IF EXISTS trg_clase_en_vivo_cohorte_activa ON clase_en_vivo ^^^
CREATE TRIGGER trg_clase_en_vivo_cohorte_activa BEFORE INSERT OR UPDATE OF id_cohorte, baja ON clase_en_vivo
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_cohorte', 'cohorte', 'id_cohorte') ^^^

DROP TRIGGER IF EXISTS trg_unidad_cronograma_programa_activo ON unidad_cronograma ^^^
CREATE TRIGGER trg_unidad_cronograma_programa_activo BEFORE INSERT OR UPDATE OF id_programa, baja ON unidad_cronograma
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_programa', 'programa', 'id_programa') ^^^

DROP TRIGGER IF EXISTS trg_unidad_cronograma_unidad_activa ON unidad_cronograma ^^^
CREATE TRIGGER trg_unidad_cronograma_unidad_activa BEFORE INSERT OR UPDATE OF id_unidad, baja ON unidad_cronograma
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_unidad', 'unidad', 'id_unidad') ^^^

DROP TRIGGER IF EXISTS trg_material_unidad_activa ON material ^^^
CREATE TRIGGER trg_material_unidad_activa BEFORE INSERT OR UPDATE OF id_unidad, baja ON material
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_unidad', 'unidad', 'id_unidad') ^^^

DROP TRIGGER IF EXISTS trg_pool_unidad_activa ON pool ^^^
CREATE TRIGGER trg_pool_unidad_activa BEFORE INSERT OR UPDATE OF id_unidad, baja ON pool
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_unidad', 'unidad', 'id_unidad') ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_unidad_activa ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_unidad_activa BEFORE INSERT OR UPDATE OF id_unidad, baja ON autoevaluacion
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_unidad', 'unidad', 'id_unidad') ^^^

DROP TRIGGER IF EXISTS trg_termino_glosario_unidad_activa ON termino_glosario ^^^
CREATE TRIGGER trg_termino_glosario_unidad_activa BEFORE INSERT OR UPDATE OF id_unidad, baja ON termino_glosario
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_unidad', 'unidad', 'id_unidad') ^^^

DROP TRIGGER IF EXISTS trg_consulta_foro_unidad_activa ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_unidad_activa BEFORE INSERT OR UPDATE OF id_unidad, baja ON consulta_foro
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_unidad', 'unidad', 'id_unidad') ^^^

DROP TRIGGER IF EXISTS trg_respuesta_foro_consulta_activa ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_consulta_activa BEFORE INSERT OR UPDATE OF id_consulta, baja ON respuesta_foro
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_consulta', 'consulta_foro', 'id_consulta') ^^^

DROP TRIGGER IF EXISTS trg_intento_autoevaluacion_autoevaluacion_activa ON intento_autoevaluacion ^^^
CREATE TRIGGER trg_intento_autoevaluacion_autoevaluacion_activa BEFORE INSERT OR UPDATE OF id_autoevaluacion, baja ON intento_autoevaluacion
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_autoevaluacion', 'autoevaluacion', 'id_autoevaluacion') ^^^
