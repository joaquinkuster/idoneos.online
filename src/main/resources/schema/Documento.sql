-- Documento: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_documento() RETURNS trigger AS $$
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

DROP TRIGGER IF EXISTS trg_documento_validar ON documento ^^^
CREATE TRIGGER trg_documento_validar BEFORE INSERT OR UPDATE ON documento
    FOR EACH ROW EXECUTE FUNCTION fn_validar_documento() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- El tamaño del archivo y el estado del OCR los actualiza el sistema solo (el OCR reemplaza el archivo por el mismo con texto):
-- no cuentan como modificaciones del recurso. Tampoco cuenta cargar el archivo de un material que genera la inteligencia artificial,
-- que nace sin archivo y se lo recibe al terminar.
DROP TRIGGER IF EXISTS trg_documento_en_uso ON documento ^^^
CREATE TRIGGER trg_documento_en_uso BEFORE UPDATE ON documento
    FOR EACH ROW WHEN (OLD.ruta_archivo IS NOT NULL
                   AND (OLD.autor IS DISTINCT FROM NEW.autor
                    OR OLD.extension IS DISTINCT FROM NEW.extension
                    OR OLD.ruta_archivo IS DISTINCT FROM NEW.ruta_archivo
                    OR OLD.paginas IS DISTINCT FROM NEW.paginas))
    EXECUTE FUNCTION fn_recurso_en_uso() ^^^

DROP TRIGGER IF EXISTS trg_documento_fuente_ia ON documento ^^^
CREATE TRIGGER trg_documento_fuente_ia BEFORE UPDATE ON documento
    FOR EACH ROW WHEN (OLD.ruta_archivo IS NOT NULL
                   AND (OLD.autor IS DISTINCT FROM NEW.autor
                    OR OLD.extension IS DISTINCT FROM NEW.extension
                    OR OLD.ruta_archivo IS DISTINCT FROM NEW.ruta_archivo
                    OR OLD.paginas IS DISTINCT FROM NEW.paginas))
    EXECUTE FUNCTION fn_documento_bloqueado_por_ia('modificar') ^^^

-- fn_documento_con_indice(): disparador diferido. Un documento tiene siempre al menos una entrada en su índice.
CREATE OR REPLACE FUNCTION fn_documento_con_indice() RETURNS trigger AS $$
DECLARE
    v_id_documento int;
BEGIN
    IF TG_TABLE_NAME = 'documento' THEN
        v_id_documento := NEW.id_documento;
    ELSE
        v_id_documento := OLD.id_documento;
    END IF;
    IF EXISTS (SELECT 1 FROM documento WHERE id_documento = v_id_documento)
       AND NOT EXISTS (SELECT 1 FROM indice_documento WHERE id_documento = v_id_documento) THEN
        RAISE EXCEPTION 'Error! El documento debe tener al menos una entrada en su índice.'
            USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NULL;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_documento_con_indice ON documento ^^^
CREATE CONSTRAINT TRIGGER trg_documento_con_indice AFTER INSERT ON documento
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_documento_con_indice() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_documento_pertenencia ON documento ^^^
CREATE TRIGGER trg_documento_pertenencia BEFORE UPDATE ON documento
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_recurso') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_documento_sin_borrado ON documento ^^^
CREATE TRIGGER trg_documento_sin_borrado BEFORE DELETE ON documento
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_documento_auditoria ON documento ^^^
CREATE TRIGGER trg_documento_auditoria AFTER INSERT OR UPDATE OR DELETE ON documento
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('Documento') ^^^
