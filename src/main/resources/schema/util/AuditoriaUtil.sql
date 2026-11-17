-- AuditoriaUtil: registro de las acciones de los usuarios (crear, modificar y eliminar).
-- Cada tabla con altas, modificaciones o bajas tiene un disparador (en el script de su entidad) que llama a fn_auditar().
-- La aplicación le pasa a la base, en cada conexión, quién es la persona con sesión iniciada y desde qué IP (idoneos.usuario e idoneos.ip).
-- Los cambios que no hace una persona (tareas programadas, el aviso de pago de MODO, la carga inicial) no se registran.

-- fn_auditar(entidad): disparador AFTER de cada tabla. Registra la acción en auditoria y, por cada columna afectada, el valor anterior y el nuevo en detalle_auditoria.
CREATE OR REPLACE FUNCTION fn_auditar() RETURNS trigger AS $$
DECLARE
    v_usuario int;
    v_ip text;
    v_accion text;
    v_tipo int;
    v_clave text;
    v_id int := 0;
    v_ant jsonb := '{}'::jsonb;
    v_nue jsonb := '{}'::jsonb;
    v_fila jsonb;
    v_auditoria int;
    v_campo text;
    v_a text;
    v_n text;
    v_cantidad int := 0;
    -- Columnas que se actualizan solas y no cuentan como un cambio del usuario
    v_ignoradas text[] := ARRAY['ultima_modificacion'];
    -- Columnas cuyo valor nunca se guarda en la auditoría
    v_secretas text[] := ARRAY['contrasena', 'token'];
BEGIN
    BEGIN
        v_usuario := nullif(current_setting('idoneos.usuario', true), '')::int;
    EXCEPTION WHEN others THEN
        v_usuario := NULL;
    END;
    IF v_usuario IS NULL THEN
        RETURN NULL;
    END IF;
    v_ip := coalesce(nullif(current_setting('idoneos.ip', true), ''), '0.0.0.0');

    IF TG_OP <> 'INSERT' THEN v_ant := to_jsonb(OLD); END IF;
    IF TG_OP <> 'DELETE' THEN v_nue := to_jsonb(NEW); END IF;
    v_fila := CASE WHEN TG_OP = 'DELETE' THEN v_ant ELSE v_nue END;

    -- Una baja lógica (baja pasa a verdadero) se registra como una eliminación
    v_accion := CASE TG_OP
        WHEN 'INSERT' THEN 'Crear'
        WHEN 'DELETE' THEN 'Eliminar'
        WHEN 'UPDATE' THEN CASE WHEN (v_ant ->> 'baja') = 'false' AND (v_nue ->> 'baja') = 'true' THEN 'Eliminar' ELSE 'Modificar' END
    END;
    SELECT id_tipo_auditoria INTO v_tipo FROM tipo_accion_auditoria WHERE nombre = v_accion;
    IF v_tipo IS NULL THEN
        RETURN NULL;
    END IF;

    -- Una modificación en la que solo cambiaron columnas automáticas no se registra
    IF TG_OP = 'UPDATE' THEN
        SELECT count(*) INTO v_cantidad FROM jsonb_object_keys(v_nue) k
            WHERE k <> ALL (v_ignoradas) AND (v_ant -> k) IS DISTINCT FROM (v_nue -> k);
        IF v_cantidad = 0 THEN
            RETURN NULL;
        END IF;
    END IF;

    -- Identificador del registro: la primera columna de la clave primaria
    BEGIN
        SELECT a.attname INTO v_clave FROM pg_index i JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = i.indkey[0]
            WHERE i.indrelid = TG_RELID AND i.indisprimary;
        v_id := coalesce((v_fila ->> v_clave)::int, 0);
    EXCEPTION WHEN others THEN
        v_id := 0;
    END;

    INSERT INTO auditoria (id_tipo_auditoria, id_usuario, entidad_afectada, id_registro, ip_usuario, fecha_hora)
        VALUES (v_tipo, v_usuario, TG_ARGV[0], v_id, left(v_ip, 45), now()) RETURNING id_auditoria INTO v_auditoria;

    FOR v_campo IN SELECT jsonb_object_keys(v_fila) LOOP
        CONTINUE WHEN v_campo = ANY (v_ignoradas);
        v_a := v_ant ->> v_campo;
        v_n := v_nue ->> v_campo;
        -- En una modificación solo se guardan las columnas que cambiaron; en un alta, las que tienen valor
        IF TG_OP = 'UPDATE' AND v_a IS NOT DISTINCT FROM v_n THEN CONTINUE; END IF;
        IF TG_OP = 'INSERT' AND v_n IS NULL THEN CONTINUE; END IF;
        IF TG_OP = 'DELETE' AND v_a IS NULL THEN CONTINUE; END IF;
        IF v_campo = ANY (v_secretas) OR v_a LIKE 'cifrado:%' OR v_n LIKE 'cifrado:%' THEN
            v_a := CASE WHEN v_a IS NULL THEN NULL ELSE '••••••••' END;
            v_n := CASE WHEN v_n IS NULL THEN NULL ELSE '••••••••' END;
        END IF;
        INSERT INTO detalle_auditoria (id_auditoria, campo, valor_anterior, valor_nuevo)
            VALUES (v_auditoria, v_campo, v_a, coalesce(v_n, ''));
    END LOOP;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^
