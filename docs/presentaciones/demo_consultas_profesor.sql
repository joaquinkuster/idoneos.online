-- ============================================================
-- IDÓNEOS ONLINE — Demo SQL para el Profesor (NOMBRES REALES)
-- Base de datos: idoneos.online | Schema: public
-- Tablas en snake_case minúscula (creadas por Hibernate)
-- ============================================================
-- Conectar con: psql -U postgres -h localhost -d "idoneos.online"
-- En pgAdmin: seleccionar la BD "idoneos.online" y usar Query Tool
-- ============================================================


-- ============================================================
-- 0. RESUMEN GENERAL — cuántos registros tiene cada tabla
-- ============================================================
SELECT 'curso'                   AS tabla, COUNT(*) AS total FROM curso
UNION ALL SELECT 'categoria',             COUNT(*) FROM categoria
UNION ALL SELECT 'cohorte',               COUNT(*) FROM cohorte
UNION ALL SELECT 'programa',              COUNT(*) FROM programa
UNION ALL SELECT 'inscripcion',           COUNT(*) FROM inscripcion
UNION ALL SELECT 'alumno',                COUNT(*) FROM alumno
UNION ALL SELECT 'docente',               COUNT(*) FROM docente
UNION ALL SELECT 'pago',                  COUNT(*) FROM pago
UNION ALL SELECT 'progreso',              COUNT(*) FROM progreso
UNION ALL SELECT 'material',              COUNT(*) FROM material
UNION ALL SELECT 'unidad',                COUNT(*) FROM unidad
UNION ALL SELECT 'cronograma',            COUNT(*) FROM cronograma
UNION ALL SELECT 'autoevaluacion',        COUNT(*) FROM autoevaluacion
UNION ALL SELECT 'intento_autoevaluacion',COUNT(*) FROM intento_autoevaluacion
UNION ALL SELECT 'consulta_foro',         COUNT(*) FROM consulta_foro
UNION ALL SELECT 'respuesta_foro',        COUNT(*) FROM respuesta_foro
ORDER BY tabla;


-- ============================================================
-- CU-01: BUSCAR CURSO
-- Pantalla: Gestión de Cursos (tarjetas)
-- ============================================================

-- Todos los cursos con categoría, nivel y docente
SELECT
    c.id_curso,
    c.nombre                                    AS curso,
    cat.nombre                                  AS categoria,
    n.nombre                                    AS nivel,
    u.nombre || ' ' || u.apellido               AS docente_titular,
    c.precio,
    c.emite_certificado,
    c.publicado,
    CASE WHEN c.baja THEN 'DADO DE BAJA' ELSE 'ACTIVO' END AS estado
FROM curso c
JOIN categoria cat ON cat.id_categoria = c.id_categoria
JOIN nivel     n   ON n.id_nivel       = c.id_nivel
JOIN docente   d   ON d.id_docente     = c.id_docente
JOIN usuario   u   ON u.id_usuario     = d.id_usuario
ORDER BY c.baja ASC, c.fecha_creacion DESC;

-- Modalidades por curso (tabla N:M: "modalidad curso" con espacio)
SELECT
    c.nombre        AS curso,
    m.nombre        AS modalidad
FROM curso c
JOIN "modalidad curso" mc ON mc.id_curso    = c.id_curso
JOIN modalidad         m  ON m.id_modalidad = mc.id_modalidad
ORDER BY c.nombre;

-- Ayudantes por curso
SELECT
    c.nombre                              AS curso,
    u.nombre || ' ' || u.apellido         AS ayudante
FROM curso c
JOIN ayudante a ON a.id_curso   = c.id_curso
JOIN docente  d ON d.id_docente = a.id_docente
JOIN usuario  u ON u.id_usuario = d.id_usuario
ORDER BY c.nombre;


-- ============================================================
-- CU-02: VER MIS CURSOS (vista del Alumno)
-- Progreso calculado en tiempo real
-- ============================================================

SELECT
    c.nombre                        AS curso,
    i.fecha                         AS fecha_inscripcion,
    i.fecha_vencimiento_acceso      AS vence,
    COUNT(p.id_progreso)            AS unidades_totales,
    COUNT(CASE WHEN p.completada THEN 1 END) AS completadas,
    ROUND(
        COUNT(CASE WHEN p.completada THEN 1 END)::numeric
        / NULLIF(COUNT(p.id_progreso), 0) * 100
    , 0) || '%'                     AS avance,
    CASE WHEN i.baja THEN 'BAJA' ELSE 'ACTIVA' END AS estado
FROM inscripcion i
JOIN cohorte    co ON co.id_cohorte  = i.id_cohorte
JOIN programa   pr ON pr.id_programa = co.id_programa
JOIN curso      c  ON c.id_curso     = pr.id_curso
LEFT JOIN progreso p ON p.id_inscripcion = i.id_inscripcion
-- WHERE i.id_alumno = 1   -- <-- descomentar con el ID del alumno
GROUP BY c.nombre, i.id_inscripcion, i.fecha, i.fecha_vencimiento_acceso, i.baja
ORDER BY i.fecha DESC;


-- ============================================================
-- CU-03: REGISTRAR CURSO — último creado
-- ============================================================

SELECT
    c.id_curso,
    c.nombre,
    c.descripcion,
    c.precio,
    cat.nombre                          AS categoria,
    n.nombre                            AS nivel,
    u.nombre || ' ' || u.apellido       AS docente_titular,
    c.fecha_creacion,
    c.baja
FROM curso c
JOIN categoria cat ON cat.id_categoria = c.id_categoria
JOIN nivel     n   ON n.id_nivel       = c.id_nivel
JOIN docente   d   ON d.id_docente     = c.id_docente
JOIN usuario   u   ON u.id_usuario     = d.id_usuario
ORDER BY c.fecha_creacion DESC
LIMIT 5;

-- Validación: docente titular que también es ayudante (debe dar 0 filas)
SELECT c.nombre AS curso, u.nombre || ' ' || u.apellido AS conflicto
FROM curso c
JOIN docente  d ON d.id_docente = c.id_docente
JOIN usuario  u ON u.id_usuario = d.id_usuario
JOIN ayudante a ON a.id_curso   = c.id_curso AND a.id_docente = c.id_docente;


-- ============================================================
-- CU-04: MODIFICAR CURSO — modo de edición
-- ============================================================

-- Sin inscripciones activas → formulario COMPLETO
SELECT c.id_curso, c.nombre, 'EDICION COMPLETA' AS modo
FROM curso c
WHERE c.baja = false
  AND NOT EXISTS (
      SELECT 1 FROM inscripcion i
      JOIN cohorte  co ON co.id_cohorte  = i.id_cohorte
      JOIN programa pr ON pr.id_programa = co.id_programa
      WHERE pr.id_curso = c.id_curso AND i.baja = false
  );

-- CON inscripciones activas → solo precio e imagen
SELECT
    c.id_curso,
    c.nombre,
    COUNT(i.id_inscripcion) AS inscriptos,
    'SOLO PRECIO E IMAGEN' AS modo
FROM curso c
JOIN programa    pr ON pr.id_curso    = c.id_curso
JOIN cohorte     co ON co.id_programa = pr.id_programa
JOIN inscripcion i  ON i.id_cohorte  = co.id_cohorte AND i.baja = false
WHERE c.baja = false
GROUP BY c.id_curso, c.nombre
ORDER BY inscriptos DESC;


-- ============================================================
-- CU-05: DAR DE BAJA CURSO — Borrado Lógico
-- ============================================================

-- Estado de todos los cursos (los dados de baja siguen en la BD)
SELECT id_curso, nombre,
    CASE WHEN baja THEN 'DADO DE BAJA' ELSE 'ACTIVO' END AS estado,
    baja
FROM curso
ORDER BY baja ASC, nombre;

-- Programas que BLOQUEAN la baja de un curso
SELECT
    c.nombre        AS curso_a_dar_de_baja,
    pr.nombre       AS programa_bloqueante
FROM curso c
JOIN programa pr ON pr.id_curso = c.id_curso AND pr.baja = false
WHERE c.baja = false
ORDER BY c.nombre;

-- DEMO borrado lógico EN VIVO (descomentar — nunca hace DELETE)
/*
UPDATE curso SET baja = true  WHERE id_curso = 1;
SELECT id_curso, nombre, baja FROM curso WHERE id_curso = 1;
-- Revertir:
UPDATE curso SET baja = false WHERE id_curso = 1;
*/


-- ============================================================
-- CU-06: CATÁLOGO PÚBLICO — cursos con inscripción abierta HOY
-- ============================================================

SELECT
    c.nombre                              AS curso,
    cat.nombre                            AS categoria,
    n.nombre                              AS nivel,
    c.precio,
    co.fecha_inicio_inscripcion           AS inscripcion_desde,
    co.fecha_fin_inscripcion              AS inscripcion_hasta,
    co.cupo_maximo,
    COUNT(i.id_inscripcion)               AS inscriptos,
    COALESCE(co.cupo_maximo, 9999) - COUNT(i.id_inscripcion) AS cupo_libre,
    u.nombre || ' ' || u.apellido         AS docente
FROM curso c
JOIN categoria   cat ON cat.id_categoria = c.id_categoria
JOIN nivel       n   ON n.id_nivel       = c.id_nivel
JOIN programa    pr  ON pr.id_curso      = c.id_curso AND pr.baja = false
JOIN cohorte     co  ON co.id_programa   = pr.id_programa
                    AND co.baja = false
                    AND NOW() BETWEEN co.fecha_inicio_inscripcion AND co.fecha_fin_inscripcion
JOIN docente     d   ON d.id_docente     = c.id_docente
JOIN usuario     u   ON u.id_usuario     = d.id_usuario
LEFT JOIN inscripcion i ON i.id_cohorte = co.id_cohorte AND i.baja = false
WHERE c.baja = false
GROUP BY c.id_curso, c.nombre, cat.nombre, n.nombre, c.precio,
         co.id_cohorte, co.fecha_inicio_inscripcion, co.fecha_fin_inscripcion,
         co.cupo_maximo, u.nombre, u.apellido
ORDER BY c.nombre;


-- ============================================================
-- CU-07/08/09/10: CATEGORÍAS
-- ============================================================

SELECT
    id_categoria,
    nombre,
    descripcion,
    fecha_creacion,
    CASE WHEN baja THEN 'DADO DE BAJA' ELSE 'ACTIVA' END AS estado
FROM categoria
ORDER BY baja ASC, nombre;

-- Categorías bloqueadas para dar de baja (tienen cursos activos)
SELECT
    cat.id_categoria,
    cat.nombre                  AS categoria,
    COUNT(c.id_curso)           AS cursos_activos,
    'BLOQUEADA' AS estado_baja
FROM categoria cat
JOIN curso c ON c.id_categoria = cat.id_categoria AND c.baja = false
WHERE cat.baja = false
GROUP BY cat.id_categoria, cat.nombre
HAVING COUNT(c.id_curso) > 0
ORDER BY cursos_activos DESC;


-- ============================================================
-- CU-11: BUSCAR COHORTE
-- ============================================================

SELECT
    co.id_cohorte,
    pr.nombre                             AS programa,
    c.nombre                              AS curso,
    co.fecha_inicio_inscripcion,
    co.fecha_fin_inscripcion,
    co.fecha_inicio_dictado,
    co.fecha_fin_dictado,
    co.cupo_maximo,
    co.semanas_acceso,
    COUNT(i.id_inscripcion)               AS inscriptos,
    CASE WHEN co.baja THEN 'CANCELADA' ELSE 'ACTIVA' END AS estado
FROM cohorte co
JOIN programa    pr ON pr.id_programa = co.id_programa
JOIN curso       c  ON c.id_curso     = pr.id_curso
LEFT JOIN inscripcion i ON i.id_cohorte = co.id_cohorte AND i.baja = false
GROUP BY co.id_cohorte, pr.nombre, c.nombre,
         co.fecha_inicio_inscripcion, co.fecha_fin_inscripcion,
         co.fecha_inicio_dictado, co.fecha_fin_dictado,
         co.cupo_maximo, co.semanas_acceso, co.baja
ORDER BY co.baja ASC, co.fecha_inicio_inscripcion DESC;


-- ============================================================
-- CU-12: REGISTRAR COHORTE
-- Validación #5: semanas_acceso >= suma de semanas_duracion del cronograma
-- ============================================================

SELECT
    pr.id_programa,
    pr.nombre                       AS programa,
    c.nombre                        AS curso,
    COUNT(cr.id)                    AS unidades,
    SUM(cr.semanas_duracion)        AS semanas_minimas_requeridas,
    'La cohorte debe tener semanas_acceso >= este valor' AS regla
FROM programa pr
JOIN curso      c  ON c.id_curso     = pr.id_curso
JOIN cronograma cr ON cr.id_programa = pr.id_programa
WHERE pr.baja = false
GROUP BY pr.id_programa, pr.nombre, c.nombre
ORDER BY semanas_minimas_requeridas DESC;


-- ============================================================
-- CU-13/14: COHORTES — cuáles están bloqueadas
-- ============================================================

-- Bloqueadas para editar o cancelar (tienen inscriptos activos)
SELECT
    co.id_cohorte,
    pr.nombre                        AS programa,
    COUNT(i.id_inscripcion)          AS inscriptos_activos,
    'BLOQUEADA para editar/cancelar' AS estado
FROM cohorte co
JOIN programa    pr ON pr.id_programa = co.id_programa
JOIN inscripcion i  ON i.id_cohorte  = co.id_cohorte AND i.baja = false
WHERE co.baja = false
GROUP BY co.id_cohorte, pr.nombre
ORDER BY inscriptos_activos DESC;

-- Alumnos que bloquean la cancelación de una cohorte (CU-14)
SELECT
    co.id_cohorte,
    pr.nombre                        AS programa,
    u.nombre || ' ' || u.apellido    AS alumno,
    i.fecha                          AS fecha_inscripcion
FROM cohorte     co
JOIN programa    pr ON pr.id_programa = co.id_programa
JOIN inscripcion i  ON i.id_cohorte  = co.id_cohorte AND i.baja = false
JOIN alumno      al ON al.id_alumno  = i.id_alumno
JOIN usuario     u  ON u.id_usuario  = al.id_usuario
ORDER BY co.id_cohorte, u.apellido;


-- ============================================================
-- CRONOGRAMA — unidades en orden (CU-23/24)
-- ============================================================

SELECT
    pr.nombre           AS programa,
    c.nombre            AS curso,
    cr.numero_orden     AS orden,
    u.titulo            AS unidad,
    cr.semanas_duracion AS semanas
FROM cronograma cr
JOIN programa pr ON pr.id_programa = cr.id_programa
JOIN unidad   u  ON u.id_unidad   = cr.id_unidad
JOIN curso    c  ON c.id_curso    = pr.id_curso
ORDER BY pr.id_programa, cr.numero_orden;


-- ============================================================
-- PROGRESO DE ALUMNOS (CU-48)
-- ============================================================

SELECT
    u.nombre || ' ' || u.apellido    AS alumno,
    c.nombre                          AS curso,
    un.titulo                         AS unidad,
    cr.numero_orden,
    p.completada,
    p.fecha_completada
FROM progreso p
JOIN inscripcion  i   ON i.id_inscripcion  = p.id_inscripcion
JOIN cohorte      co  ON co.id_cohorte     = i.id_cohorte
JOIN programa     pr  ON pr.id_programa    = co.id_programa
JOIN cronograma   cr  ON cr.id_programa    = pr.id_programa
                     AND cr.id_unidad      = p.id_unidad
JOIN curso        c   ON c.id_curso        = pr.id_curso
JOIN unidad       un  ON un.id_unidad      = p.id_unidad
JOIN alumno       al  ON al.id_alumno      = i.id_alumno
JOIN usuario      u   ON u.id_usuario      = al.id_usuario
ORDER BY u.apellido, c.nombre, cr.numero_orden;


-- ============================================================
-- AUDITORÍA (CU-95)
-- Nota: la tabla auditoria ya tiene los campos valor_anterior/valor_nuevo
-- (Hibernate fusionó DetalleAuditoria en la misma tabla)
-- ============================================================

SELECT
    a.id_auditoria,
    t.nombre                              AS tipo_accion,
    a.entidad_afectada                    AS tabla,
    a.id_afectado,
    u.nombre || ' ' || u.apellido         AS responsable,
    a.ip_usuario                          AS ip,
    a.valor_anterior                      AS antes,
    a.valor_nuevo                         AS despues,
    a.fecha_hora
FROM auditoria a
JOIN tipo_accion_auditoria t ON t.id_tipo_accion_auditoria = a.id_tipo_accion_auditoria
JOIN usuario               u ON u.id_usuario               = a.id_usuario
ORDER BY a.fecha_hora DESC
LIMIT 30;


-- ============================================================
-- CADENA COMPLETA — Curso → Programa → Cohorte → Inscripción → Alumno
-- ============================================================

SELECT
    c.nombre                              AS curso,
    pr.nombre                             AS programa,
    co.fecha_inicio_inscripcion           AS cohorte_desde,
    u.nombre || ' ' || u.apellido         AS alumno,
    i.fecha                               AS fecha_inscripcion,
    i.fecha_vencimiento_acceso            AS vence,
    CASE WHEN i.baja THEN 'BAJA' ELSE 'ACTIVA' END AS estado
FROM curso      c
JOIN programa    pr ON pr.id_curso     = c.id_curso
JOIN cohorte     co ON co.id_programa  = pr.id_programa
JOIN inscripcion i  ON i.id_cohorte   = co.id_cohorte
JOIN alumno      al ON al.id_alumno   = i.id_alumno
JOIN usuario     u  ON u.id_usuario   = al.id_usuario
ORDER BY c.nombre, co.fecha_inicio_inscripcion, u.apellido;

-- ============================================================
-- FIN — Idóneos Online | Küster & Martínez 2026
-- ============================================================
