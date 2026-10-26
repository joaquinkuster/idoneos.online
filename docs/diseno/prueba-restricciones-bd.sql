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
CREATE OR REPLACE FUNCTION pg_temp.probar_v2(sentencia text, esperada text) RETURNS text AS $f$
DECLARE
    filas int;
    restriccion text;
BEGIN
    BEGIN
        -- Para probar un CHECK solo, se apagan los disparadores durante la prueba (los CHECK siguen activos).
        -- Sin esto, los disparadores de datos congelados rechazan primero el cambio. Se revierte solo.
        IF esperada LIKE 'ck\_%' THEN
            PERFORM set_config('session_replication_role', 'replica', true);
        END IF;
        EXECUTE sentencia;
        GET DIAGNOSTICS filas = ROW_COUNT;
        IF filas = 0 THEN RAISE EXCEPTION 'SIN_FILAS'; END IF;
        RAISE EXCEPTION 'NO_RECHAZADO';
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS restriccion = CONSTRAINT_NAME;
        IF SQLERRM = 'SIN_FILAS' THEN RETURN 'SIN DATOS PARA PROBAR'; END IF;
        IF SQLERRM = 'NO_RECHAZADO' THEN
            IF esperada = 'ACEPTA' THEN RETURN 'OK: aceptado (es un cambio permitido)'; END IF;
            RETURN 'FALLA: la base acepto el dato';
        END IF;
        IF esperada = 'unique' AND SQLSTATE = '23505' THEN RETURN 'OK: rechazado por UNIQUE'; END IF;
        IF restriccion = esperada THEN RETURN 'OK: rechazado por ' || restriccion; END IF;
        RETURN 'FALLA: ' || SQLSTATE || ' ' || SQLERRM;
    END;
END
$f$ LANGUAGE plpgsql;

-- IMPORTANTE: ejecutar el script completo (incluida la funcion del paso 2). Si se ejecuta solo el paso 3, pgAdmin
-- usa la version anterior de la funcion que quedo guardada en la sesion y los casos "ACEPTA" dan FALLA.
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
  $$UPDATE autoevaluacion SET fecha_cierre = fecha_apertura WHERE id_autoevaluacion = (SELECT min(id_autoevaluacion) FROM autoevaluacion)$$, 'ck_autoevaluacion_cierre'),
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
    SELECT id_curso, id_docente, false, now(), false FROM docente_curso WHERE baja = false LIMIT 1$$, 'uq_docente_curso_curso_docente_activo'),
 -- Bajas con dependientes activos (disparadores de baja). Cada una se deshace sola.
 ('CU-10: baja de una categoria con cursos activos',
  $$UPDATE categoria SET baja = true WHERE id_categoria = (SELECT id_categoria FROM curso WHERE baja = false LIMIT 1)$$, 'trg_categoria_baja'),
 ('CU-05: baja de un curso con programas o unidades activas',
  $$UPDATE curso SET baja = true WHERE id_curso = (SELECT id_curso FROM programa WHERE baja = false LIMIT 1)$$, 'trg_curso_baja'),
 ('CU-18: baja de un programa con cohortes activas',
  $$UPDATE programa SET baja = true WHERE id_programa = (SELECT id_programa FROM cohorte WHERE baja = false LIMIT 1)$$, 'trg_programa_baja'),
 ('CU-14: baja de una cohorte con inscripciones activas',
  $$UPDATE cohorte SET baja = true WHERE id_cohorte = (SELECT id_cohorte FROM inscripcion WHERE baja = false LIMIT 1)$$, 'trg_cohorte_baja'),
 ('CU-22: baja de una unidad que esta en un cronograma activo',
  $$UPDATE unidad SET baja = true WHERE id_unidad = (SELECT id_unidad FROM unidad_cronograma WHERE baja = false LIMIT 1)$$, 'trg_unidad_baja'),
 ('CU-59: baja de un descuento ya usado',
  $$UPDATE descuento SET baja = true WHERE id_descuento = (SELECT id_descuento FROM descuento WHERE cantidad_usada > 0 AND baja = false LIMIT 1)$$, 'trg_descuento_baja'),
 ('CU-63: baja de un pool asociado a una autoevaluacion activa',
  $$UPDATE pool SET baja = true WHERE id_pool = (SELECT ap.id_pool FROM autoevaluacion_pool ap JOIN autoevaluacion a ON a.id_autoevaluacion = ap.id_autoevaluacion WHERE a.baja = false LIMIT 1)$$, 'trg_pool_baja'),
 ('CU-67: baja de una autoevaluacion con intentos activos',
  $$UPDATE autoevaluacion SET baja = true WHERE id_autoevaluacion = (SELECT id_autoevaluacion FROM intento_autoevaluacion WHERE baja = false LIMIT 1)$$, 'trg_autoevaluacion_baja'),
 ('CU-04: desvincular un docente que tiene material activo en el curso',
  $$UPDATE docente_curso SET baja = true WHERE id_docente_curso = (SELECT id_docente_curso FROM material WHERE baja = false LIMIT 1)$$, 'trg_docente_curso_baja'),
 -- Datos congelados (disparadores de modificacion). "ACEPTA" = cambio permitido que la base debe dejar pasar.
 ('CU-04: cambiar el nombre de un curso con inscripciones activas',
  $$UPDATE curso SET nombre = nombre || ' x' WHERE id_curso = (SELECT p.id_curso FROM inscripcion i JOIN cohorte c ON c.id_cohorte = i.id_cohorte JOIN programa p ON p.id_programa = c.id_programa WHERE i.baja = false LIMIT 1)$$, 'trg_curso_congelado'),
 ('CU-04: cambiar el precio de un curso con inscripciones activas (permitido)',
  $$UPDATE curso SET precio = precio + 1 WHERE id_curso = (SELECT p.id_curso FROM inscripcion i JOIN cohorte c ON c.id_cohorte = i.id_cohorte JOIN programa p ON p.id_programa = c.id_programa WHERE i.baja = false LIMIT 1)$$, 'ACEPTA'),
 ('CU-04: quitar una modalidad de un curso con inscripciones activas',
  $$DELETE FROM curso_modalidad WHERE id_curso_modalidad = (SELECT id_curso_modalidad FROM curso_modalidad WHERE fn_curso_con_inscripciones(id_curso) LIMIT 1)$$, 'trg_curso_modalidad_congelado'),
 ('CU-09: cambiar el nombre de una categoria con inscripciones activas',
  $$UPDATE categoria SET nombre = nombre || ' x' WHERE id_categoria = (SELECT cu.id_categoria FROM inscripcion i JOIN cohorte c ON c.id_cohorte = i.id_cohorte JOIN programa p ON p.id_programa = c.id_programa JOIN curso cu ON cu.id_curso = p.id_curso WHERE i.baja = false LIMIT 1)$$, 'trg_categoria_congelado'),
 ('CU-13: cambiar el cupo de una cohorte con inscripciones activas',
  $$UPDATE cohorte SET cupo_maximo = coalesce(cupo_maximo, 0) + 1 WHERE id_cohorte = (SELECT id_cohorte FROM inscripcion WHERE baja = false LIMIT 1)$$, 'trg_cohorte_congelado'),
 ('CU-17: cambiar el nombre de un programa con inscripciones activas',
  $$UPDATE programa SET nombre = nombre || ' x' WHERE id_programa = (SELECT c.id_programa FROM inscripcion i JOIN cohorte c ON c.id_cohorte = i.id_cohorte WHERE i.baja = false LIMIT 1)$$, 'trg_programa_congelado'),
 ('CU-21: cambiar el titulo de una unidad de un curso con inscripciones activas',
  $$UPDATE unidad SET titulo = titulo || ' x' WHERE id_unidad = (SELECT u.id_unidad FROM unidad u WHERE u.baja = false AND fn_curso_con_inscripciones(u.id_curso) LIMIT 1)$$, 'trg_unidad_congelado'),
 ('CU-25: cambiar la duracion de una unidad del cronograma de un programa con inscripciones',
  $$UPDATE unidad_cronograma SET semanas_duracion = semanas_duracion + 1 WHERE id_unidad_cronograma = (SELECT uc.id_unidad_cronograma FROM unidad_cronograma uc WHERE uc.baja = false AND fn_programa_con_inscripciones(uc.id_programa) LIMIT 1)$$, 'trg_unidad_cronograma_congelado'),
 ('CU-62: cambiar el nombre de un pool con intentos activos',
  $$UPDATE pool SET nombre = nombre || ' x' WHERE id_pool = (SELECT ap.id_pool FROM autoevaluacion_pool ap JOIN intento_autoevaluacion i ON i.id_autoevaluacion = ap.id_autoevaluacion WHERE i.baja = false LIMIT 1)$$, 'trg_pool_congelado'),
 ('CU-62: cambiar el texto de una pregunta de un pool con intentos activos',
  $$UPDATE pregunta SET texto = texto || ' x' WHERE id_pregunta = (SELECT pr.id_pregunta FROM pregunta pr WHERE pr.baja = false AND fn_pool_con_intentos(pr.id_pool) LIMIT 1)$$, 'trg_pregunta_congelado'),
 ('CU-62: cambiar el texto de una opcion de un pool con intentos activos',
  $$UPDATE opcion_respuesta SET texto = texto || ' x' WHERE id_opcion_respuesta = (SELECT o.id_opcion_respuesta FROM opcion_respuesta o JOIN pregunta pr ON pr.id_pregunta = o.id_pregunta WHERE o.baja = false AND fn_pool_con_intentos(pr.id_pool) LIMIT 1)$$, 'trg_opcion_respuesta_congelado'),
 ('CU-66: cambiar el nombre de una autoevaluacion con intentos activos',
  $$UPDATE autoevaluacion SET nombre = nombre || ' x' WHERE id_autoevaluacion = (SELECT id_autoevaluacion FROM intento_autoevaluacion WHERE baja = false LIMIT 1)$$, 'trg_autoevaluacion_congelado'),
 ('CU-66: ocultar una autoevaluacion con intentos activos (permitido)',
  $$UPDATE autoevaluacion SET oculto = NOT oculto WHERE id_autoevaluacion = (SELECT id_autoevaluacion FROM intento_autoevaluacion WHERE baja = false LIMIT 1)$$, 'ACEPTA'),
 ('CU-66: acortar el cierre de una autoevaluacion con intentos activos',
  $$UPDATE autoevaluacion SET fecha_cierre = now() WHERE id_autoevaluacion = (SELECT id_autoevaluacion FROM intento_autoevaluacion WHERE baja = false LIMIT 1) AND fecha_cierre IS NULL$$, 'trg_autoevaluacion_congelado'),
 ('CU-66: cambiar los pools de una autoevaluacion con intentos activos',
  $$DELETE FROM autoevaluacion_pool WHERE id_autoevaluacion = (SELECT id_autoevaluacion FROM intento_autoevaluacion WHERE baja = false LIMIT 1)$$, 'trg_autoevaluacion_pool_congelado')
)
SELECT descripcion, esperada AS restriccion_esperada, pg_temp.probar_v2(sentencia, esperada) AS resultado
FROM casos;
