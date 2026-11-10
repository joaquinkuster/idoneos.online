-- Bibliografia: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_bibliografia() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF NEW.paginas IS NOT NULL AND NEW.paginas <= 0 THEN
            v_mensaje := fn_msg_formato('La cantidad de páginas', 'número entero mayor a cero');
        ELSIF NEW.tamanio_bytes IS NOT NULL AND NEW.tamanio_bytes < 0 THEN
            v_mensaje := fn_msg_formato('El tamaño del archivo', 'número desde 0');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_bibliografia_validar ON bibliografia ^^^
CREATE TRIGGER trg_bibliografia_validar BEFORE INSERT OR UPDATE ON bibliografia
    FOR EACH ROW EXECUTE FUNCTION fn_validar_bibliografia() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_bibliografia_en_uso ON bibliografia ^^^
CREATE TRIGGER trg_bibliografia_en_uso BEFORE UPDATE ON bibliografia
    FOR EACH ROW WHEN (OLD.autor IS DISTINCT FROM NEW.autor
                    OR OLD.editorial IS DISTINCT FROM NEW.editorial
                    OR OLD.isbn IS DISTINCT FROM NEW.isbn
                    OR OLD.fecha_publicacion IS DISTINCT FROM NEW.fecha_publicacion
                    OR OLD.extension IS DISTINCT FROM NEW.extension
                    OR OLD.ruta_archivo IS DISTINCT FROM NEW.ruta_archivo
                    OR OLD.paginas IS DISTINCT FROM NEW.paginas)
    EXECUTE FUNCTION fn_recurso_en_uso() ^^^

-- No se admiten dos bibliografias vigentes con el mismo ISBN (RecursoService.validarIsbnNoRepetido).

-- fn_bibliografia_isbn(): disparador de las bibliografias. Rechaza un ISBN que ya tiene otra bibliografia vigente.
CREATE OR REPLACE FUNCTION fn_bibliografia_isbn() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM bibliografia b JOIN recurso r ON r.id_recurso = b.id_recurso
               WHERE r.baja = false AND b.id_recurso <> NEW.id_recurso
                 AND upper(replace(replace(b.isbn, '-', ''), ' ', '')) = upper(replace(replace(NEW.isbn, '-', ''), ' ', ''))) THEN
        RAISE EXCEPTION 'Error! Ya existe una bibliografía registrada con ese ISBN.'
            USING ERRCODE = 'unique_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_bibliografia_isbn ON bibliografia ^^^
CREATE TRIGGER trg_bibliografia_isbn BEFORE INSERT OR UPDATE OF isbn ON bibliografia
    FOR EACH ROW EXECUTE FUNCTION fn_bibliografia_isbn() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_bibliografia_pertenencia ON bibliografia ^^^
CREATE TRIGGER trg_bibliografia_pertenencia BEFORE UPDATE ON bibliografia
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_recurso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_bibliografia_sin_borrado ON bibliografia ^^^
CREATE TRIGGER trg_bibliografia_sin_borrado BEFORE DELETE ON bibliografia
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
