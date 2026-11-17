-- MaterialDidactico: reglas de la base de datos de esta entidad.

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

-- Cambiar la categoría de un material que ya está en un tema cuenta como modificarlo. Los datos del archivo los controla el documento.
DROP TRIGGER IF EXISTS trg_material_didactico_en_uso ON material_didactico ^^^
CREATE TRIGGER trg_material_didactico_en_uso BEFORE UPDATE ON material_didactico
    FOR EACH ROW WHEN (OLD.id_categoria_material_didactico IS DISTINCT FROM NEW.id_categoria_material_didactico)
    EXECUTE FUNCTION fn_recurso_en_uso_de_subtipo() ^^^

DROP TRIGGER IF EXISTS trg_material_didactico_fuente_ia ON material_didactico ^^^
CREATE TRIGGER trg_material_didactico_fuente_ia BEFORE UPDATE ON material_didactico
    FOR EACH ROW WHEN (OLD.id_categoria_material_didactico IS DISTINCT FROM NEW.id_categoria_material_didactico)
    EXECUTE FUNCTION fn_documento_bloqueado_por_ia('modificar') ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_material_didactico_pertenencia ON material_didactico ^^^
CREATE TRIGGER trg_material_didactico_pertenencia BEFORE UPDATE ON material_didactico
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_documento') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_material_didactico_sin_borrado ON material_didactico ^^^
CREATE TRIGGER trg_material_didactico_sin_borrado BEFORE DELETE ON material_didactico
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_material_didactico_auditoria ON material_didactico ^^^
CREATE TRIGGER trg_material_didactico_auditoria AFTER INSERT OR UPDATE OR DELETE ON material_didactico
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('MaterialDidactico') ^^^
