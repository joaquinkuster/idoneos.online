-- PublicacionUtil Cuándo algo está publicado en el catálogo o congelado por una cohorte publicada.

-- Publicacion en el catalogo.

-- fn_cohorte_esta_publicada(p_id_cohorte): funcion auxiliar. Devuelve true si la cohorte esta publicada en el catalogo.
CREATE OR REPLACE FUNCTION fn_cohorte_esta_publicada(p_id_cohorte int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM cohorte c
                   JOIN estado_cohorte e ON e.id_estado_cohorte = c.id_estado_cohorte
                   WHERE c.id_cohorte = p_id_cohorte AND c.baja = false
                     AND e.nombre NOT IN ('Pendiente de contenido', 'Cancelada por falta de contenido'))
$$ LANGUAGE sql STABLE ^^^

-- fn_curso_tiene_cohortes_publicadas(p_id_curso): funcion auxiliar. Devuelve true si el curso tiene alguna cohorte publicada
-- (Curso.tieneCohortesPublicadas).
CREATE OR REPLACE FUNCTION fn_curso_tiene_cohortes_publicadas(p_id_curso int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM cohorte c
                   JOIN estado_cohorte e ON e.id_estado_cohorte = c.id_estado_cohorte
                   WHERE c.id_curso = p_id_curso AND c.baja = false
                     AND e.nombre NOT IN ('Pendiente de contenido', 'Cancelada por falta de contenido'))
$$ LANGUAGE sql STABLE ^^^

-- fn_programa_esta_congelado(p_id_programa): funcion auxiliar.
CREATE OR REPLACE FUNCTION fn_programa_esta_congelado(p_id_programa int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM cohorte c
                   JOIN estado_cohorte e ON e.id_estado_cohorte = c.id_estado_cohorte
                   WHERE c.id_programa = p_id_programa AND c.baja = false
                     AND e.nombre NOT IN ('Pendiente de contenido', 'Cancelada por falta de contenido'))
$$ LANGUAGE sql STABLE ^^^

-- fn_categoria_tiene_cursos_publicados(p_id_categoria): funcion auxiliar. Devuelve true si algun curso activo de la categoria
-- tiene cohortes publicadas (CategoriaService.tieneCursosConCohortesPublicadas).
CREATE OR REPLACE FUNCTION fn_categoria_tiene_cursos_publicados(p_id_categoria int) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM curso cu
                   WHERE cu.id_categoria = p_id_categoria AND cu.baja = false
                     AND fn_curso_tiene_cohortes_publicadas(cu.id_curso))
$$ LANGUAGE sql STABLE ^^^

-- Datos de los mensajes

-- Cohorte.getEtiqueta(): el mes y el año en que comienza (el del dictado o, si no tiene, el de la inscripcion) y su estado.
-- Por ejemplo "Marzo 2026 (En dictado)".
CREATE OR REPLACE FUNCTION fn_cohorte_etiqueta(p_id_cohorte int) RETURNS text AS $$
DECLARE
    v_meses text[] := ARRAY['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    v_inicio timestamp;
    v_estado text;
BEGIN
    SELECT coalesce(c.fecha_inicio_dictado, c.fecha_inicio_inscripcion), e.nombre INTO v_inicio, v_estado
        FROM cohorte c JOIN estado_cohorte e ON e.id_estado_cohorte = c.id_estado_cohorte
        WHERE c.id_cohorte = p_id_cohorte;
    RETURN v_meses[extract(month FROM v_inicio)::int] || ' ' || extract(year FROM v_inicio)::int || ' (' || v_estado || ')';
END
$$ LANGUAGE plpgsql STABLE ^^^
