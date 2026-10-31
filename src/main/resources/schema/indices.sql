-- Indices de la base de datos (solo PostgreSQL).
-- Unicos parciales: la unicidad vale solo entre los registros activos. De apoyo: aceleran las consultas de
-- dependientes que hacen los disparadores.
-- Spring ejecuta todos los scripts de esta carpeta (ver application.properties) despues de que Hibernate crea las tablas.
-- Cada archivo define sus propias funciones, por eso el orden entre ellos no importa.
-- Cada sentencia termina con ^^^ (y no con punto y coma) porque los cuerpos de las funciones llevan punto y coma adentro.
-- Se puede ejecutar mas de una vez: usa IF NOT EXISTS, CREATE OR REPLACE y DROP ... IF EXISTS.

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
-- (Tema por curso, Pool por tema y TerminoGlosario por tema ya los cubre un UNIQUE existente.)
-- ---------------------------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS ix_curso_categoria_activo ON curso (id_categoria) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_programa_curso_activo ON programa (id_curso) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_cohorte_programa_activo ON cohorte (id_programa) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_inscripcion_cohorte_activo ON inscripcion (id_cohorte) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_clase_en_vivo_cohorte_activo ON clase_en_vivo (id_cohorte) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_tema_recurso_tema_activo ON tema_recurso (id_tema) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_autoevaluacion_tema_activo ON autoevaluacion (id_tema) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_intento_autoevaluacion_autoevaluacion_activo ON intento_autoevaluacion (id_autoevaluacion) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_consulta_foro_tema_activo ON consulta_foro (id_tema) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_respuesta_foro_consulta_activo ON respuesta_foro (id_consulta) WHERE baja = false ^^^
CREATE INDEX IF NOT EXISTS ix_tema_cronograma_tema_activo ON tema_cronograma (id_tema) WHERE baja = false ^^^
-- AutoevaluacionPool no tiene baja: se consulta por pool para saber si esta en alguna autoevaluacion
CREATE INDEX IF NOT EXISTS ix_autoevaluacion_pool_pool ON autoevaluacion_pool (id_pool) ^^^
