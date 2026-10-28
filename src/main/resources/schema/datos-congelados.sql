-- Disparadores de datos congelados (solo PostgreSQL): lo que el alumno vio, pago o rindio no se modifica.
-- Spring ejecuta todos los scripts de esta carpeta (ver application.properties) despues de que Hibernate crea las tablas.
-- Cada archivo define sus propias funciones, por eso el orden entre ellos no importa.
-- Cada sentencia termina con ^^^ (y no con punto y coma) porque los cuerpos de las funciones llevan punto y coma adentro.
-- Se puede ejecutar mas de una vez: usa IF NOT EXISTS, CREATE OR REPLACE y DROP ... IF EXISTS.

-- ---------------------------------------------------------------------------------------------
-- Disparadores de datos congelados: lo que el alumno vio, pago o rindio no se puede modificar.
-- "Activo" significa que no fue dado de baja (baja = false).
-- ---------------------------------------------------------------------------------------------

-- fn_curso_con_inscripciones(p_id_curso): funcion auxiliar. Devuelve true si el curso tiene alguna inscripcion activa,
-- buscandola a traves de sus programas y cohortes.
CREATE OR REPLACE FUNCTION fn_curso_con_inscripciones(p_id_curso int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM inscripcion i
                   JOIN cohorte c ON c.id_cohorte = i.id_cohorte
                   JOIN programa p ON p.id_programa = c.id_programa
                   WHERE p.id_curso = p_id_curso AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

-- fn_categoria_con_inscripciones(p_id_categoria): funcion auxiliar. Devuelve true si algun curso de la categoria tiene
-- inscripciones activas.
CREATE OR REPLACE FUNCTION fn_categoria_con_inscripciones(p_id_categoria int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM inscripcion i
                   JOIN cohorte c ON c.id_cohorte = i.id_cohorte
                   JOIN programa p ON p.id_programa = c.id_programa
                   JOIN curso cu ON cu.id_curso = p.id_curso
                   WHERE cu.id_categoria = p_id_categoria AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

-- fn_programa_con_inscripciones(p_id_programa): funcion auxiliar. Devuelve true si alguna cohorte del programa tiene
-- inscripciones activas.
CREATE OR REPLACE FUNCTION fn_programa_con_inscripciones(p_id_programa int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM inscripcion i
                   JOIN cohorte c ON c.id_cohorte = i.id_cohorte
                   WHERE c.id_programa = p_id_programa AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

-- fn_autoevaluacion_con_intentos(p_id_autoevaluacion): funcion auxiliar. Devuelve true si la autoevaluacion tiene algun
-- intento activo (un intento dado de baja por fraude no cuenta).
CREATE OR REPLACE FUNCTION fn_autoevaluacion_con_intentos(p_id_autoevaluacion int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM intento_autoevaluacion
                   WHERE id_autoevaluacion = p_id_autoevaluacion AND baja = false)
$$ LANGUAGE sql STABLE ^^^

-- fn_pool_con_intentos(p_id_pool): funcion auxiliar. Devuelve true si alguna autoevaluacion que usa el pool tiene intentos
-- activos. Un pool con intentos ya fue rendido por algun alumno y no se puede alterar.
CREATE OR REPLACE FUNCTION fn_pool_con_intentos(p_id_pool int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM intento_autoevaluacion i
                   JOIN autoevaluacion_pool ap ON ap.id_autoevaluacion = i.id_autoevaluacion
                   WHERE ap.id_pool = p_id_pool AND i.baja = false)
$$ LANGUAGE sql STABLE ^^^

-- fn_curso_congelado() (CU-04): disparador de modificacion de un curso. El disparador solo la llama si cambio el nombre,
-- la descripcion, la categoria, el nivel o si emite certificado; en ese caso rechaza el cambio cuando el curso tiene
-- inscripciones activas. El precio, la imagen y el equipo docente se pueden modificar siempre.
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

-- fn_curso_modalidad_congelada() (CU-04): disparador de las modalidades de un curso. Rechaza agregar, cambiar o quitar una
-- modalidad cuando el curso tiene inscripciones activas.
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

-- fn_categoria_congelada() (CU-09): disparador de modificacion de una categoria. Se llama solo si cambio el nombre o la
-- descripcion y rechaza el cambio si alguno de sus cursos tiene inscripciones activas.
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

-- fn_cohorte_congelada() (CU-13): disparador de modificacion de una cohorte. Se llama solo si cambio alguna fecha, el cupo,
-- las semanas de acceso o el programa, y rechaza el cambio si la cohorte tiene inscripciones activas.
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

-- fn_programa_congelado() (CU-17): disparador de modificacion de un programa. Se llama solo si cambio el nombre, la
-- descripcion, los objetivos, la bibliografia o el curso, y rechaza el cambio si el programa tiene inscripciones activas.
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

-- fn_tema_congelada() (CU-21): disparador de modificacion de un tema. Se llama solo si cambio el titulo, la descripcion,
-- el contenido o el curso, y rechaza el cambio si el curso del tema tiene inscripciones activas.
CREATE OR REPLACE FUNCTION fn_tema_congelada() RETURNS trigger AS $$
BEGIN
    IF fn_curso_con_inscripciones(OLD.id_curso) THEN
        RAISE EXCEPTION 'No se puede modificar el tema: el curso tiene inscripciones activas'
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

-- fn_tema_cronograma_congelado() (CU-25): disparador del cronograma de un programa. Rechaza agregar o quitar temas,
-- cambiar su orden o su duracion cuando el programa tiene inscripciones activas. Si una modificacion no toca esas
-- columnas, la deja pasar.
CREATE OR REPLACE FUNCTION fn_tema_cronograma_congelado() RETURNS trigger AS $$
DECLARE
    v_programa int;
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.id_programa IS NOT DISTINCT FROM NEW.id_programa
       AND OLD.id_tema IS NOT DISTINCT FROM NEW.id_tema
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

DROP TRIGGER IF EXISTS trg_tema_cronograma_congelado ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_congelado BEFORE INSERT OR UPDATE OR DELETE ON tema_cronograma
    FOR EACH ROW EXECUTE FUNCTION fn_tema_cronograma_congelado() ^^^

-- fn_pool_congelado() (CU-62): disparador de modificacion de un pool. Se llama solo si cambio el nombre o el tema, y
-- rechaza el cambio si el pool tiene intentos activos.
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
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre OR OLD.id_tema IS DISTINCT FROM NEW.id_tema)
    EXECUTE FUNCTION fn_pool_congelado() ^^^

-- fn_pregunta_congelada() (CU-62): disparador de las preguntas de un pool. Rechaza agregar, modificar o dar de baja una
-- pregunta cuando el pool tiene intentos activos. Si la modificacion no toca el texto, el tipo, la baja ni el pool, la deja pasar.
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

-- fn_opcion_respuesta_congelada() (CU-62): disparador de las opciones de respuesta. Busca el pool de la pregunta y rechaza
-- agregar, modificar o dar de baja una opcion cuando ese pool tiene intentos activos.
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

-- fn_autoevaluacion_congelada() (CU-66): disparador de modificacion de una autoevaluacion. Si tiene intentos activos, rechaza
-- cambiar su contenido (nombre, tiempo limite, cantidad de preguntas, nota minima, apertura, descuento por error,
-- navegacion libre, permiso para salir y tema) y tambien acortar la fecha de cierre o reducir los intentos permitidos.
-- Lo que si se permite: ocultarla o mostrarla, extender el cierre y ampliar los intentos.
CREATE OR REPLACE FUNCTION fn_autoevaluacion_congelada() RETURNS trigger AS $$
DECLARE
    v_contenido boolean;
    v_retroceso boolean;
BEGIN
    v_contenido := OLD.id_tema IS DISTINCT FROM NEW.id_tema
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

-- fn_autoevaluacion_pool_congelado() (CU-66): disparador de los pools de una autoevaluacion. Rechaza agregar, cambiar o quitar
-- pools cuando la autoevaluacion tiene intentos activos.
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
