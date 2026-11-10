-- Categoria: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_categoria() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.nombre) = '' THEN
            v_mensaje := 'Error! El nombre de la categoría no puede quedar vacío.';
        ELSIF NEW.descripcion IS NOT NULL AND btrim(NEW.descripcion) = '' THEN
            v_mensaje := fn_msg_vacio('La descripción');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_categoria_validar ON categoria ^^^
CREATE TRIGGER trg_categoria_validar BEFORE INSERT OR UPDATE ON categoria
    FOR EACH ROW EXECUTE FUNCTION fn_validar_categoria() ^^^

-- ----------------------------------------------------------------------
-- Datos congelados
-- ----------------------------------------------------------------------

-- fn_categoria_congelada(): disparador de modificacion de una categoria. Se llama solo si cambio el nombre o la descripcion y
-- rechaza el cambio si alguno de sus cursos tiene cohortes publicadas.
CREATE OR REPLACE FUNCTION fn_categoria_congelada() RETURNS trigger AS $$
BEGIN
    IF fn_categoria_tiene_cursos_publicados(OLD.id_categoria) THEN
        RAISE EXCEPTION '%', fn_msg_publicacion('la categoría', OLD.nombre, 'hay cursos con cohortes publicadas en el catálogo', fn_txt_cierre_diseno())
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_categoria_congelado ON categoria ^^^
CREATE TRIGGER trg_categoria_congelado BEFORE UPDATE ON categoria
    FOR EACH ROW WHEN (OLD.nombre IS DISTINCT FROM NEW.nombre OR OLD.descripcion IS DISTINCT FROM NEW.descripcion)
    EXECUTE FUNCTION fn_categoria_congelada() ^^^

-- ----------------------------------------------------------------------
-- Bajas
-- ----------------------------------------------------------------------

-- Disparadores de baja: se activan solo cuando baja pasa de false a true.

-- fn_baja_categoria(): una categoria no se da de baja si tiene cursos activos.
CREATE OR REPLACE FUNCTION fn_baja_categoria() RETURNS trigger AS $$
DECLARE
    v_cursos bigint;
BEGIN
    SELECT count(*) INTO v_cursos FROM curso WHERE id_categoria = OLD.id_categoria AND baja = false;
    IF v_cursos > 0 THEN
        RAISE EXCEPTION '%', fn_msg_baja('la categoría', OLD.nombre,
                'tiene ' || fn_msg_cantidad(v_cursos, 'curso activo asociado', 'cursos activos asociados'),
                'Dé de baja primero ' || CASE WHEN v_cursos = 1 THEN 'ese curso.' ELSE 'esos cursos.' END)
            USING ERRCODE = 'restrict_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_categoria_baja ON categoria ^^^
CREATE TRIGGER trg_categoria_baja BEFORE UPDATE OF baja ON categoria
    FOR EACH ROW WHEN (OLD.baja = false AND NEW.baja = true)
    EXECUTE FUNCTION fn_baja_categoria() ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_categoria_sin_borrado ON categoria ^^^
CREATE TRIGGER trg_categoria_sin_borrado BEFORE DELETE ON categoria
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
