-- DuracionUtil Duraciones escritas en palabras, como DuracionUtil.java.

-- DuracionUtil.formatear(): meses si es multiplo de 30 dias, semanas si es multiplo de 7 y dias en cualquier otro caso
CREATE OR REPLACE FUNCTION fn_msg_duracion(p_dias int) RETURNS text AS $$
    SELECT CASE
        WHEN p_dias > 0 AND p_dias % 30 = 0 THEN fn_msg_cantidad(p_dias / 30, 'mes', 'meses')
        WHEN p_dias > 0 AND p_dias % 7 = 0 THEN fn_msg_cantidad(p_dias / 7, 'semana', 'semanas')
        ELSE fn_msg_cantidad(p_dias, 'día', 'días')
    END
$$ LANGUAGE sql IMMUTABLE ^^^

-- DuracionUtil.formatearMinutos(): horas si es multiplo de 60 minutos, "1 h 30 min" si pasa de una hora y minutos en cualquier otro caso
CREATE OR REPLACE FUNCTION fn_msg_minutos(p_minutos int) RETURNS text AS $$
    SELECT CASE
        WHEN p_minutos > 0 AND p_minutos % 60 = 0 THEN fn_msg_cantidad(p_minutos / 60, 'hora', 'horas')
        WHEN p_minutos > 60 THEN (p_minutos / 60) || ' h ' || (p_minutos % 60) || ' min'
        ELSE fn_msg_cantidad(p_minutos, 'minuto', 'minutos')
    END
$$ LANGUAGE sql IMMUTABLE ^^^
