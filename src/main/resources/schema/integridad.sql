-- Integridad entre registros (solo PostgreSQL): columnas que no cambian y coherencia padre-hijo.
-- Spring ejecuta todos los scripts de esta carpeta (ver application.properties) despues de que Hibernate crea las tablas.
-- Cada archivo define sus propias funciones, por eso el orden entre ellos no importa.
-- Cada sentencia termina con ^^^ (y no con punto y coma) porque los cuerpos de las funciones llevan punto y coma adentro.
-- Se puede ejecutar mas de una vez: usa IF NOT EXISTS, CREATE OR REPLACE y DROP ... IF EXISTS.

-- ---------------------------------------------------------------------------------------------
-- Columnas que nunca cambian despues del alta (CU-38, 42, 47 y 48). Argumentos: las columnas protegidas.
-- ---------------------------------------------------------------------------------------------
-- fn_columnas_inmutables(): funcion generica para columnas que no cambian despues del alta. Recibe como argumentos los nombres
-- de las columnas protegidas, compara el valor anterior con el nuevo de cada una y, si alguna cambio, rechaza la modificacion.
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
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_tema', 'id_inscripcion') ^^^

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
-- fn_padre_activo(): funcion generica de coherencia padre-hijo. Se ejecuta al crear un registro o al cambiar su padre o su baja.
-- Recibe la columna del hijo que apunta al padre, la tabla del padre y la columna clave del padre. Si el padre esta dado de
-- baja, rechaza que el hijo quede activo.
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

DROP TRIGGER IF EXISTS trg_tema_curso_activo ON tema ^^^
CREATE TRIGGER trg_tema_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON tema
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

DROP TRIGGER IF EXISTS trg_docente_curso_curso_activo ON docente_curso ^^^
CREATE TRIGGER trg_docente_curso_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON docente_curso
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

DROP TRIGGER IF EXISTS trg_cohorte_curso_activo ON cohorte ^^^
CREATE TRIGGER trg_cohorte_curso_activo BEFORE INSERT OR UPDATE OF id_curso, baja ON cohorte
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_curso', 'curso', 'id_curso') ^^^

DROP TRIGGER IF EXISTS trg_cohorte_programa_activo ON cohorte ^^^
CREATE TRIGGER trg_cohorte_programa_activo BEFORE INSERT OR UPDATE OF id_programa, baja ON cohorte
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_programa', 'programa', 'id_programa') ^^^

DROP TRIGGER IF EXISTS trg_clase_en_vivo_cohorte_activa ON clase_en_vivo ^^^
CREATE TRIGGER trg_clase_en_vivo_cohorte_activa BEFORE INSERT OR UPDATE OF id_cohorte, baja ON clase_en_vivo
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_cohorte', 'cohorte', 'id_cohorte') ^^^

DROP TRIGGER IF EXISTS trg_tema_cronograma_programa_activo ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_programa_activo BEFORE INSERT OR UPDATE OF id_programa, baja ON tema_cronograma
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_programa', 'programa', 'id_programa') ^^^

DROP TRIGGER IF EXISTS trg_tema_cronograma_tema_activa ON tema_cronograma ^^^
CREATE TRIGGER trg_tema_cronograma_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON tema_cronograma
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

DROP TRIGGER IF EXISTS trg_material_tema_activa ON material ^^^
CREATE TRIGGER trg_material_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON material
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

DROP TRIGGER IF EXISTS trg_pool_tema_activa ON pool ^^^
CREATE TRIGGER trg_pool_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON pool
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

DROP TRIGGER IF EXISTS trg_autoevaluacion_tema_activa ON autoevaluacion ^^^
CREATE TRIGGER trg_autoevaluacion_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON autoevaluacion
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

DROP TRIGGER IF EXISTS trg_termino_glosario_tema_activa ON termino_glosario ^^^
CREATE TRIGGER trg_termino_glosario_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON termino_glosario
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

DROP TRIGGER IF EXISTS trg_consulta_foro_tema_activa ON consulta_foro ^^^
CREATE TRIGGER trg_consulta_foro_tema_activa BEFORE INSERT OR UPDATE OF id_tema, baja ON consulta_foro
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_tema', 'tema', 'id_tema') ^^^

DROP TRIGGER IF EXISTS trg_respuesta_foro_consulta_activa ON respuesta_foro ^^^
CREATE TRIGGER trg_respuesta_foro_consulta_activa BEFORE INSERT OR UPDATE OF id_consulta, baja ON respuesta_foro
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_consulta', 'consulta_foro', 'id_consulta') ^^^

DROP TRIGGER IF EXISTS trg_intento_autoevaluacion_autoevaluacion_activa ON intento_autoevaluacion ^^^
CREATE TRIGGER trg_intento_autoevaluacion_autoevaluacion_activa BEFORE INSERT OR UPDATE OF id_autoevaluacion, baja ON intento_autoevaluacion
    FOR EACH ROW WHEN (NEW.baja = false) EXECUTE FUNCTION fn_padre_activo('id_autoevaluacion', 'autoevaluacion', 'id_autoevaluacion') ^^^
