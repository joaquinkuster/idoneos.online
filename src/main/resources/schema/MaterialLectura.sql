-- MaterialLectura: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_material_lectura() RETURNS trigger AS $$
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

DROP TRIGGER IF EXISTS trg_material_lectura_validar ON material_lectura ^^^
CREATE TRIGGER trg_material_lectura_validar BEFORE INSERT OR UPDATE ON material_lectura
    FOR EACH ROW EXECUTE FUNCTION fn_validar_material_lectura() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- El tamaño del archivo y el estado del OCR los actualiza el sistema solo (el OCR reemplaza el archivo por el mismo con texto):
-- no cuentan como modificaciones del recurso.
DROP TRIGGER IF EXISTS trg_material_lectura_en_uso ON material_lectura ^^^
CREATE TRIGGER trg_material_lectura_en_uso BEFORE UPDATE ON material_lectura
    FOR EACH ROW WHEN (OLD.autor IS DISTINCT FROM NEW.autor
                    OR OLD.extension IS DISTINCT FROM NEW.extension
                    OR OLD.ruta_archivo IS DISTINCT FROM NEW.ruta_archivo
                    OR OLD.paginas IS DISTINCT FROM NEW.paginas
                    OR OLD.id_categoria_material_lectura IS DISTINCT FROM NEW.id_categoria_material_lectura)
    EXECUTE FUNCTION fn_recurso_en_uso() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_material_lectura_pertenencia ON material_lectura ^^^
CREATE TRIGGER trg_material_lectura_pertenencia BEFORE UPDATE ON material_lectura
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_recurso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_material_lectura_sin_borrado ON material_lectura ^^^
CREATE TRIGGER trg_material_lectura_sin_borrado BEFORE DELETE ON material_lectura
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
