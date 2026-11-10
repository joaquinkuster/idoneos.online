-- IntegridadUtil Funciones genéricas de integridad: columnas que no cambian, padre activo, sin borrado.

-- Columnas que nunca cambian despues del alta.
CREATE OR REPLACE FUNCTION fn_columnas_inmutables() RETURNS trigger AS $$
DECLARE
    i int;
BEGIN
    FOR i IN 0 .. TG_NARGS - 1 LOOP
        IF (to_jsonb(OLD) ->> TG_ARGV[i]) IS DISTINCT FROM (to_jsonb(NEW) ->> TG_ARGV[i]) THEN
            RAISE EXCEPTION 'No se puede modificar la columna % de %', TG_ARGV[i], TG_TABLE_NAME
                USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
        END IF;
    END LOOP;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

-- Coherencia padre-hijo: no se crea ni se reactiva un registro activo dentro de uno dado de baja.
CREATE OR REPLACE FUNCTION fn_padre_activo() RETURNS trigger AS $$
DECLARE
    v_baja boolean;
BEGIN
    EXECUTE format('SELECT baja FROM %I WHERE %I = $1::int', TG_ARGV[1], TG_ARGV[2])
        INTO v_baja USING (to_jsonb(NEW) ->> TG_ARGV[0]);
    IF v_baja IS TRUE THEN
        RAISE EXCEPTION 'No se puede crear ni reactivar el registro: su % esta dado de baja', TG_ARGV[1]
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

-- Valores predefinidos: niveles, modalidades, roles, metodos de pago, tipos y estados.

-- fn_catalogo_predefinido(): disparador de los valores predefinidos. Rechaza cualquier cambio fuera de la carga inicial.
CREATE OR REPLACE FUNCTION fn_catalogo_predefinido() RETURNS trigger AS $$
BEGIN
    IF current_setting('idoneos.carga_inicial', true) = 'on' THEN
        IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Error! Los valores de «%» están predefinidos y no se pueden agregar, modificar ni borrar.', replace(TG_TABLE_NAME, '_', ' ')
        USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
END
$$ LANGUAGE plpgsql ^^^

-- Historial: la auditoria solo se inserta. Lo que se registro no se altera.

-- fn_historial(): disparador de las tablas de historial. Rechaza modificar un registro ya guardado.
CREATE OR REPLACE FUNCTION fn_historial() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Error! Los registros de «%» son un historial y no se pueden modificar.', replace(TG_TABLE_NAME, '_', ' ')
        USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
END
$$ LANGUAGE plpgsql ^^^

-- Sin borrados: todas las bajas del sistema son logicas, asi que no se borra ningun registro.

-- fn_sin_borrado(): disparador de borrado. Rechaza borrar un registro.
CREATE OR REPLACE FUNCTION fn_sin_borrado() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Error! Los registros de «%» no se borran: se dan de baja.', replace(TG_TABLE_NAME, '_', ' ')
        USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
END
$$ LANGUAGE plpgsql ^^^
