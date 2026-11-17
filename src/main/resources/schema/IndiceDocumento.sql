-- IndiceDocumento: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: rangos, formatos y textos no vacíos (los mismos que los CHECK del modelo, con el mensaje del servicio).
CREATE OR REPLACE FUNCTION fn_validar_indice_documento() RETURNS trigger AS $$
DECLARE
    v_mensaje text;
BEGIN
        IF btrim(NEW.titulo) = '' THEN
            v_mensaje := fn_msg_vacio('El título de la entrada del índice');
        ELSIF NEW.nivel < 1 OR NEW.nivel > 4 THEN
            v_mensaje := fn_msg_formato('El nivel de la entrada del índice', 'número entero de 1 a 4');
        ELSIF NEW.pagina < 1 THEN
            v_mensaje := fn_msg_formato('La página de la entrada del índice', 'número entero desde 1');
        END IF;
    IF v_mensaje IS NOT NULL THEN
        RAISE EXCEPTION '%', v_mensaje USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_indice_documento_validar ON indice_documento ^^^
CREATE TRIGGER trg_indice_documento_validar BEFORE INSERT OR UPDATE ON indice_documento
    FOR EACH ROW EXECUTE FUNCTION fn_validar_indice_documento() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_indice_documento_pertenencia ON indice_documento ^^^
CREATE TRIGGER trg_indice_documento_pertenencia BEFORE UPDATE ON indice_documento
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_documento') ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- El índice se guarda completo cada vez que cambia (y las oraciones se acomodan a él), así que sus entradas se pueden borrar. Lo que no puede pasar
-- es que un documento quede sin ninguna: se comprueba al cerrar la operación, para poder reemplazarlas todas dentro de la misma.
DROP TRIGGER IF EXISTS trg_indice_documento_con_entradas ON indice_documento ^^^
CREATE CONSTRAINT TRIGGER trg_indice_documento_con_entradas AFTER DELETE OR UPDATE OF id_documento ON indice_documento
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_documento_con_indice() ^^^

-- El índice de un documento que es fuente o evidencia de contenido generado con IA vigente no se reemplaza: las oraciones de las que salieron las
-- preguntas dependen de él.
DROP TRIGGER IF EXISTS trg_indice_documento_fuente_ia ON indice_documento ^^^
CREATE TRIGGER trg_indice_documento_fuente_ia BEFORE DELETE ON indice_documento
    FOR EACH ROW EXECUTE FUNCTION fn_documento_bloqueado_por_ia('modificar') ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_indice_documento_auditoria ON indice_documento ^^^
CREATE TRIGGER trg_indice_documento_auditoria AFTER INSERT OR UPDATE OR DELETE ON indice_documento
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('IndiceDocumento') ^^^
