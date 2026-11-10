-- MensajeErrorUtil Mensajes de error: los mismos textos que arma MensajeErrorUtil.java.

-- Textos fijos (MensajeErrorUtil)

-- MOTIVO_PUBLICADO: cuando el registro es el propio curso, un programa o su cronograma
CREATE OR REPLACE FUNCTION fn_txt_motivo_publicado() RETURNS text AS $$
    SELECT 'tiene cohortes publicadas en el catálogo'::text
$$ LANGUAGE sql IMMUTABLE ^^^

-- MOTIVO_CURSO_PUBLICADO: cuando el bloqueo lo causa el curso al que pertenece el registro
CREATE OR REPLACE FUNCTION fn_txt_motivo_curso_publicado() RETURNS text AS $$
    SELECT 'el curso tiene cohortes publicadas en el catálogo'::text
$$ LANGUAGE sql IMMUTABLE ^^^

-- COMPROMISO_PUBLICADO: por que lo publicado no se toca
CREATE OR REPLACE FUNCTION fn_txt_compromiso_publicado() RETURNS text AS $$
    SELECT 'Lo publicado es un compromiso de la empresa con los alumnos que lo consultan o ya se inscribieron, y modificarlo afectaría lo que ellos tuvieron en cuenta al inscribirse.'::text
$$ LANGUAGE sql IMMUTABLE ^^^

-- CIERRE_DISENO: cierre del mensaje de un programa, cronograma, tema o categoria
CREATE OR REPLACE FUNCTION fn_txt_cierre_diseno() RETURNS text AS $$
    SELECT 'Si necesitás otro diseño, solicitá al administrador la baja de la cohorte para crearla de nuevo.'::text
$$ LANGUAGE sql IMMUTABLE ^^^

-- CIERRE_CURSO: cierre del mensaje de un curso
CREATE OR REPLACE FUNCTION fn_txt_cierre_curso() RETURNS text AS $$
    SELECT 'Solo podés modificar el precio, la portada y el equipo docente. Si necesitás otro diseño, solicitá al administrador la baja de la cohorte para crearla de nuevo.'::text
$$ LANGUAGE sql IMMUTABLE ^^^

-- CIERRE_COHORTE: cierre del mensaje de una cohorte publicada
CREATE OR REPLACE FUNCTION fn_txt_cierre_cohorte() RETURNS text AS $$
    SELECT 'Si necesitás otro diseño, solicitá al administrador su baja para crearla de nuevo.'::text
$$ LANGUAGE sql IMMUTABLE ^^^

-- Armado de mensajes (MensajeErrorUtil)

-- formatearCantidad(): "1 curso activo asociado" o "3 cursos activos asociados"
CREATE OR REPLACE FUNCTION fn_msg_cantidad(p_cantidad bigint, p_singular text, p_plural text) RETURNS text AS $$
    SELECT p_cantidad || ' ' || CASE WHEN p_cantidad = 1 THEN p_singular ELSE p_plural END
$$ LANGUAGE sql IMMUTABLE ^^^

-- armarAccionBloqueada(): Error! No se puede {accion} {tipo} "{nombre}": {motivo}. {que hacer}
CREATE OR REPLACE FUNCTION fn_msg_accion(p_accion text, p_tipo text, p_nombre text, p_motivo text, p_que_hacer text) RETURNS text AS $$
    SELECT 'Error! No se puede ' || p_accion || ' ' || p_tipo || ' «' || p_nombre || '»: ' || p_motivo || '.'
           || CASE WHEN p_que_hacer IS NULL OR btrim(p_que_hacer) = '' THEN '' ELSE ' ' || p_que_hacer END
$$ LANGUAGE sql IMMUTABLE ^^^

-- armarBajaBloqueada(): Error! No se puede dar de baja {tipo} "{nombre}": {motivo}. {que hacer}
CREATE OR REPLACE FUNCTION fn_msg_baja(p_tipo text, p_nombre text, p_motivo text, p_que_hacer text) RETURNS text AS $$
    SELECT 'Error! No se puede dar de baja ' || p_tipo || ' «' || p_nombre || '»: ' || p_motivo || '.'
           || CASE WHEN p_que_hacer IS NULL OR btrim(p_que_hacer) = '' THEN '' ELSE ' ' || p_que_hacer END
$$ LANGUAGE sql IMMUTABLE ^^^

-- bloqueoPorPublicacion(): lo que no se puede modificar porque el curso tiene cohortes publicadas en el catalogo
CREATE OR REPLACE FUNCTION fn_msg_publicacion(p_tipo text, p_nombre text, p_motivo text, p_cierre text) RETURNS text AS $$
    SELECT fn_msg_accion('modificar', p_tipo, p_nombre, p_motivo, fn_txt_compromiso_publicado() || ' ' || p_cierre)
$$ LANGUAGE sql IMMUTABLE ^^^

-- motivoDisenoIncompleto(): el diseño del curso pasaria de completo a incompleto
CREATE OR REPLACE FUNCTION fn_msg_diseno_incompleto(p_detalle text) RETURNS text AS $$
    SELECT p_detalle || ' y el programa del curso quedaría incompleto'
$$ LANGUAGE sql IMMUTABLE ^^^

-- armarFormatoInvalido(): Error! {sujeto} no es válido. Formato válido: {formato}. El verbo y la terminacion siguen el articulo del sujeto
-- (El, La, Los, Las).
CREATE OR REPLACE FUNCTION fn_msg_formato(p_sujeto text, p_formato text) RETURNS text AS $$
    SELECT 'Error! ' || p_sujeto || ' ' || CASE WHEN p_sujeto LIKE 'Los %' OR p_sujeto LIKE 'Las %' THEN 'no son' ELSE 'no es' END
           || ' válid' || CASE WHEN p_sujeto LIKE 'La %' OR p_sujeto LIKE 'Las %' THEN 'a' ELSE 'o' END
           || CASE WHEN p_sujeto LIKE 'Los %' OR p_sujeto LIKE 'Las %' THEN 's' ELSE '' END
           || '. Formato válido: ' || p_formato || '.'
$$ LANGUAGE sql IMMUTABLE ^^^

-- Un dato obligatorio que quedo vacio
CREATE OR REPLACE FUNCTION fn_msg_vacio(p_sujeto text) RETURNS text AS $$
    SELECT 'Error! ' || p_sujeto || CASE WHEN p_sujeto LIKE 'Los %' OR p_sujeto LIKE 'Las %' THEN ' no pueden quedar vac' ELSE ' no puede quedar vac' END
           || CASE WHEN p_sujeto LIKE 'La %' OR p_sujeto LIKE 'Las %' THEN 'ía' ELSE 'ío' END
           || CASE WHEN p_sujeto LIKE 'Los %' OR p_sujeto LIKE 'Las %' THEN 's' ELSE '' END || '.'
$$ LANGUAGE sql IMMUTABLE ^^^
