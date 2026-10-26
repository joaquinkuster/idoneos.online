-- Prueba de las restricciones de la base de datos (validaciones simples, Fase 1).
-- Se ejecuta en pgAdmin (Query Tool) sobre la base idoneos.online, DESPUES de haber levantado la aplicacion
-- al menos una vez (Hibernate crea las tablas y la semilla carga los datos).
-- No deja ningun cambio en la base: cada prueba se deshace sola.

-- 1) Restricciones CHECK creadas (se esperan 33)
SELECT conrelid::regclass AS tabla, conname AS restriccion, pg_get_constraintdef(oid) AS condicion
FROM pg_constraint
WHERE conname LIKE 'ck\_%'
ORDER BY 1, 2;

-- 1b) Indices creados: 2 unicos parciales (uq_) y 12 de apoyo (ix_), 14 en total
-- (pgAdmin muestra solo el ultimo resultado: seleccionar esta consulta y ejecutar solo la seleccion)
SELECT tablename AS tabla, indexname AS indice, indexdef AS definicion
FROM pg_indexes
WHERE indexname LIKE 'uq\_%' OR indexname LIKE 'ix\_%'
ORDER BY 1, 2;

-- 2) Funcion auxiliar: ejecuta una sentencia que DEBERIA ser rechazada y verifica por que restriccion.
CREATE OR REPLACE FUNCTION pg_temp.probar(sentencia text, esperada text) RETURNS text AS $f$
DECLARE
    filas int;
    restriccion text;
BEGIN
    BEGIN
        EXECUTE sentencia;
        GET DIAGNOSTICS filas = ROW_COUNT;
        IF filas = 0 THEN RAISE EXCEPTION 'SIN_FILAS'; END IF;
        RAISE EXCEPTION 'NO_RECHAZADO';
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS restriccion = CONSTRAINT_NAME;
        IF SQLERRM = 'SIN_FILAS' THEN RETURN 'SIN DATOS PARA PROBAR'; END IF;
        IF SQLERRM = 'NO_RECHAZADO' THEN RETURN 'FALLA: la base acepto el dato'; END IF;
        IF esperada = 'unique' AND SQLSTATE = '23505' THEN RETURN 'OK: rechazado por UNIQUE'; END IF;
        IF restriccion = esperada THEN RETURN 'OK: rechazado por ' || restriccion; END IF;
        RETURN 'FALLA: ' || SQLSTATE || ' ' || SQLERRM;
    END;
END
$f$ LANGUAGE plpgsql;

-- 3) Pruebas: todas las filas deben decir "OK"
WITH casos(descripcion, sentencia, esperada) AS (VALUES
 ('Descuento con porcentaje 150',
  $$INSERT INTO descuento (nombre, cursos_requeridos, porcentaje, vigencia_desde, vigencia_hasta, cantidad_limite, cantidad_usada, fecha_creacion, baja)
    VALUES ('prueba', 0, 150, now(), now() + interval '1 day', 5, 0, now(), false)$$, 'ck_descuento_porcentaje'),
 ('Descuento con vigencia hasta anterior a desde',
  $$INSERT INTO descuento (nombre, cursos_requeridos, porcentaje, vigencia_desde, vigencia_hasta, cantidad_limite, cantidad_usada, fecha_creacion, baja)
    VALUES ('prueba', 0, 10, now(), now() - interval '1 day', 5, 0, now(), false)$$, 'ck_descuento_vigencia'),
 ('Descuento con cantidad limite 0',
  $$INSERT INTO descuento (nombre, cursos_requeridos, porcentaje, vigencia_desde, vigencia_hasta, cantidad_limite, cantidad_usada, fecha_creacion, baja)
    VALUES ('prueba', 0, 10, now(), now() + interval '1 day', 0, 0, now(), false)$$, 'ck_descuento_limite'),
 ('Descuento con cursos requeridos negativos',
  $$INSERT INTO descuento (nombre, cursos_requeridos, porcentaje, vigencia_desde, vigencia_hasta, cantidad_limite, cantidad_usada, fecha_creacion, baja)
    VALUES ('prueba', -1, 10, now(), now() + interval '1 day', 5, 0, now(), false)$$, 'ck_descuento_cursos'),
 ('Descuento con cantidad usada mayor al limite',
  $$INSERT INTO descuento (nombre, cursos_requeridos, porcentaje, vigencia_desde, vigencia_hasta, cantidad_limite, cantidad_usada, fecha_creacion, baja)
    VALUES ('prueba', 0, 10, now(), now() + interval '1 day', 5, 6, now(), false)$$, 'ck_descuento_uso'),
 ('Descuento con nombre de solo espacios',
  $$INSERT INTO descuento (nombre, cursos_requeridos, porcentaje, vigencia_desde, vigencia_hasta, cantidad_limite, cantidad_usada, fecha_creacion, baja)
    VALUES ('   ', 0, 10, now(), now() + interval '1 day', 5, 0, now(), false)$$, 'ck_descuento_nombre'),
 ('Categoria con nombre de solo espacios',
  $$INSERT INTO categoria (nombre, fecha_creacion, baja) VALUES ('   ', now(), false)$$, 'ck_categoria_nombre'),
 ('Categoria con nombre repetido (UNIQUE)',
  $$INSERT INTO categoria (nombre, fecha_creacion, baja) SELECT nombre, now(), false FROM categoria LIMIT 1$$, 'unique'),
 ('Curso con precio negativo',
  $$UPDATE curso SET precio = -1 WHERE id_curso = (SELECT min(id_curso) FROM curso)$$, 'ck_curso_precio'),
 ('Curso con nombre de solo espacios',
  $$UPDATE curso SET nombre = '   ' WHERE id_curso = (SELECT min(id_curso) FROM curso)$$, 'ck_curso_nombre'),
 ('Cohorte con fin de inscripcion igual al inicio',
  $$UPDATE cohorte SET fecha_fin_inscripcion = fecha_inicio_inscripcion WHERE id_cohorte = (SELECT min(id_cohorte) FROM cohorte)$$, 'ck_cohorte_inscripcion'),
 ('Cohorte con fin de dictado igual al inicio',
  $$UPDATE cohorte SET fecha_fin_dictado = fecha_inicio_dictado WHERE id_cohorte = (SELECT min(id_cohorte) FROM cohorte WHERE fecha_inicio_dictado IS NOT NULL AND fecha_fin_dictado IS NOT NULL)$$, 'ck_cohorte_dictado'),
 ('Cohorte con inicio de dictado anterior al fin de inscripcion',
  $$UPDATE cohorte SET fecha_inicio_dictado = fecha_fin_inscripcion - interval '1 day' WHERE id_cohorte = (SELECT min(id_cohorte) FROM cohorte WHERE fecha_inicio_dictado IS NOT NULL)$$, 'ck_cohorte_inicio_dictado'),
 ('Cohorte con cupo maximo 0',
  $$UPDATE cohorte SET cupo_maximo = 0 WHERE id_cohorte = (SELECT min(id_cohorte) FROM cohorte WHERE cupo_maximo IS NOT NULL)$$, 'ck_cohorte_cupo'),
 ('Cohorte con semanas de acceso 0',
  $$UPDATE cohorte SET semanas_acceso = 0 WHERE id_cohorte = (SELECT min(id_cohorte) FROM cohorte)$$, 'ck_cohorte_semanas'),
 ('Autoevaluacion con nota minima 11',
  $$UPDATE autoevaluacion SET nota_minima = 11 WHERE id_autoevaluacion = (SELECT min(id_autoevaluacion) FROM autoevaluacion)$$, 'ck_autoevaluacion_nota'),
 ('Autoevaluacion con 0 preguntas',
  $$UPDATE autoevaluacion SET cantidad_preguntas = 0 WHERE id_autoevaluacion = (SELECT min(id_autoevaluacion) FROM autoevaluacion)$$, 'ck_autoevaluacion_preguntas'),
 ('Autoevaluacion con tiempo limite 0',
  $$UPDATE autoevaluacion SET tiempo_limite = 0 WHERE id_autoevaluacion = (SELECT min(id_autoevaluacion) FROM autoevaluacion)$$, 'ck_autoevaluacion_tiempo'),
 ('Autoevaluacion con 0 intentos permitidos',
  $$UPDATE autoevaluacion SET intentos_permitidos = 0 WHERE id_autoevaluacion = (SELECT min(id_autoevaluacion) FROM autoevaluacion)$$, 'ck_autoevaluacion_intentos'),
 ('Autoevaluacion con cierre igual a la apertura',
  $$UPDATE autoevaluacion SET fecha_cierre = fecha_apertura WHERE id_autoevaluacion = (SELECT min(id_autoevaluacion) FROM autoevaluacion WHERE fecha_cierre IS NOT NULL)$$, 'ck_autoevaluacion_cierre'),
 ('Pago con monto negativo',
  $$UPDATE pago SET monto = -1 WHERE id_pago = (SELECT min(id_pago) FROM pago)$$, 'ck_pago_monto'),
 ('Cronograma con duracion 0 semanas',
  $$UPDATE unidad_cronograma SET semanas_duracion = 0 WHERE id_unidad_cronograma = (SELECT min(id_unidad_cronograma) FROM unidad_cronograma)$$, 'ck_unidad_cronograma_semanas'),
 ('Unidad con titulo de solo espacios',
  $$UPDATE unidad SET titulo = '   ' WHERE id_unidad = (SELECT min(id_unidad) FROM unidad)$$, 'ck_unidad_titulo'),
 ('Programa con nombre de solo espacios',
  $$UPDATE programa SET nombre = '   ' WHERE id_programa = (SELECT min(id_programa) FROM programa)$$, 'ck_programa_nombre'),
 ('Consulta de foro con texto de solo espacios',
  $$UPDATE consulta_foro SET texto = '   ' WHERE id_consulta = (SELECT min(id_consulta) FROM consulta_foro)$$, 'ck_consulta_foro_texto'),
 ('Inscripcion repetida del mismo alumno a la misma cohorte',
  $$INSERT INTO inscripcion (id_cohorte, id_alumno, fecha_creacion, baja)
    SELECT id_cohorte, id_alumno, now(), false FROM inscripcion WHERE baja = false LIMIT 1$$, 'uq_inscripcion_alumno_cohorte_activo'),
 ('Docente repetido en el mismo curso',
  $$INSERT INTO docente_curso (id_curso, id_docente, es_titular, fecha_creacion, baja)
    SELECT id_curso, id_docente, false, now(), false FROM docente_curso WHERE baja = false LIMIT 1$$, 'uq_docente_curso_curso_docente_activo')
)
SELECT descripcion, esperada AS restriccion_esperada, pg_temp.probar(sentencia, esperada) AS resultado
FROM casos;
