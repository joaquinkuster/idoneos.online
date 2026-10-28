-- Disparadores de baja (solo PostgreSQL): no se da de baja un registro con dependientes activos.
-- Spring ejecuta todos los scripts de esta carpeta (ver application.properties) despues de que Hibernate crea las tablas.
-- Cada archivo define sus propias funciones, por eso el orden entre ellos no importa.
-- Cada sentencia termina con ^^^ (y no con punto y coma) porque los cuerpos de las funciones llevan punto y coma adentro.
-- Se puede ejecutar mas de una vez: usa IF NOT EXISTS, CREATE OR REPLACE y DROP ... IF EXISTS.

-- ---------------------------------------------------------------------------------------------
-- Disparadores de baja: no se da de baja un registro que tiene registros activos que dependen de el.
-- Se activan solo cuando baja pasa de false a true. Si hay dependientes, la base rechaza el cambio.
-- ---------------------------------------------------------------------------------------------

-- fn_baja_sin_dependientes(): funcion generica de los disparadores de baja. Se ejecuta antes de que un registro pase a
-- estar dado de baja. Recibe como argumentos la columna clave del registro y, a continuacion, cada tabla que depende de
-- el con el formato tabla:columna. Cuenta en cada tabla los registros activos (baja = false) que apuntan al registro
-- que se da de baja y, si encuentra alguno, rechaza la baja con un error que indica en que tabla estan.
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

-- CU-05: un curso no se da de baja si tiene programas o temas activos
DROP TRIGGER IF EXISTS trg_curso_baja ON curso ^^^
CREATE TRIGGER trg_curso_baja BEFORE UPDATE OF baja ON curso
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_sin_dependientes('id_curso', 'programa:id_curso', 'tema:id_curso') ^^^

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

-- fn_baja_tema(): disparador de baja de un tema (CU-22). Rechaza la baja si el tema esta en el cronograma de un
-- programa activo, si tiene pools o autoevaluaciones activos, o si el curso al que pertenece tiene inscripciones activas.
CREATE OR REPLACE FUNCTION fn_baja_tema() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM tema_cronograma uc
               JOIN programa p ON p.id_programa = uc.id_programa
               WHERE uc.id_tema = OLD.id_tema AND uc.baja = false AND p.baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja el tema: pertenece al cronograma de un programa activo'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF EXISTS (SELECT 1 FROM pool WHERE id_tema = OLD.id_tema AND baja = false)
       OR EXISTS (SELECT 1 FROM autoevaluacion WHERE id_tema = OLD.id_tema AND baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja el tema: tiene pools o autoevaluaciones activos'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    IF EXISTS (SELECT 1 FROM inscripcion i
               JOIN cohorte c ON c.id_cohorte = i.id_cohorte
               JOIN programa p ON p.id_programa = c.id_programa
               WHERE p.id_curso = OLD.id_curso AND i.baja = false) THEN
        RAISE EXCEPTION 'No se puede dar de baja el tema: el curso tiene inscripciones activas'
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_tema_baja ON tema ^^^
CREATE TRIGGER trg_tema_baja BEFORE UPDATE OF baja ON tema
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_tema() ^^^

-- fn_baja_descuento(): disparador de baja de un descuento (CU-59). Rechaza la baja si el descuento ya se uso
-- (cantidad_usada mayor que cero).
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

-- fn_baja_pool(): disparador de baja de un pool (CU-63). Rechaza la baja si el pool esta asociado a una autoevaluacion
-- activa (una autoevaluacion dada de baja no cuenta).
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
