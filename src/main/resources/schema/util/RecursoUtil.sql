-- RecursoUtil Modalidad de un recurso y regla de los recursos que algún tema usa.

-- Los datos de un recurso (titulo, descripcion, archivo, autor, ISBN) no se modifican mientras algun tema lo usa.

-- fn_recurso_en_uso(): disparador de modificacion de un recurso o de los datos propios de su tipo. Rechaza el cambio si algun tema activo
-- usa el recurso.
CREATE OR REPLACE FUNCTION fn_recurso_en_uso() RETURNS trigger AS $$
DECLARE
    v_usos bigint;
    v_titulo text;
BEGIN
    SELECT count(*) INTO v_usos FROM tema_recurso WHERE id_recurso = OLD.id_recurso AND baja = false;
    IF v_usos >= 1 THEN
        SELECT titulo INTO v_titulo FROM recurso WHERE id_recurso = OLD.id_recurso;
        RAISE EXCEPTION '%', fn_msg_accion('modificar', 'el recurso', v_titulo,
                'tiene ' || fn_msg_cantidad(v_usos, 'tema asociado', 'temas asociados'), '')
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

-- Registrar un recurso en un tema, y publicarlo

-- fn_recurso_modalidad(p_id_recurso): funcion auxiliar.
CREATE OR REPLACE FUNCTION fn_recurso_modalidad(p_id_recurso int) RETURNS text AS $$
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM grabacion g WHERE g.id_recurso = p_id_recurso) THEN
            CASE WHEN EXISTS (SELECT 1 FROM clase_en_vivo c JOIN grabacion g ON g.id_grabacion = c.id_grabacion
                              WHERE g.id_recurso = p_id_recurso AND c.baja = false)
                 THEN 'En vivo' ELSE 'Grabada' END
        WHEN EXISTS (SELECT 1 FROM video_clon v WHERE v.id_recurso = p_id_recurso) THEN 'Con clon de IA'
    END
$$ LANGUAGE sql STABLE ^^^

-- fn_curso_incluye_modalidad(p_id_curso, p_modalidad): funcion auxiliar. Devuelve true si el curso tiene la modalidad.
CREATE OR REPLACE FUNCTION fn_curso_incluye_modalidad(p_id_curso int, p_modalidad text) RETURNS boolean AS $$
    SELECT EXISTS (SELECT 1 FROM curso_modalidad cm JOIN modalidad m ON m.id_modalidad = cm.id_modalidad
                   WHERE cm.id_curso = p_id_curso AND m.nombre = p_modalidad)
$$ LANGUAGE sql STABLE ^^^
