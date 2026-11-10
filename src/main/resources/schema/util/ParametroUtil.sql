-- ParametroUtil Lectura de los parámetros del sistema.

-- fn_parametro_entero(): el valor entero de un parametro del sistema, o el que se indica si no esta cargado.
CREATE OR REPLACE FUNCTION fn_parametro_entero(p_clave text, p_defecto int) RETURNS int AS $$
    SELECT coalesce((SELECT btrim(valor)::int FROM parametro WHERE clave = p_clave), p_defecto)
$$ LANGUAGE sql STABLE ^^^
