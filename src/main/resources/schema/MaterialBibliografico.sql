-- MaterialBibliografico: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Validaciones
-- ----------------------------------------------------------------------

-- Validaciones: el archivo es obligatorio (la subida lo exige con el mismo mensaje).
CREATE OR REPLACE FUNCTION fn_validar_material_bibliografico() RETURNS trigger AS $$
BEGIN
    IF (SELECT ruta_archivo FROM documento WHERE id_documento = NEW.id_documento) IS NULL THEN
        RAISE EXCEPTION 'Error! Debe seleccionar el archivo del material bibliográfico.'
            USING ERRCODE = 'check_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_material_bibliografico_validar ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_validar BEFORE INSERT ON material_bibliografico
    FOR EACH ROW EXECUTE FUNCTION fn_validar_material_bibliografico() ^^^

-- ----------------------------------------------------------------------
-- Reglas de negocio
-- ----------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_material_bibliografico_en_uso ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_en_uso BEFORE UPDATE ON material_bibliografico
    FOR EACH ROW WHEN (OLD.publicado_en IS DISTINCT FROM NEW.publicado_en
                    OR OLD.identificador IS DISTINCT FROM NEW.identificador
                    OR OLD.fecha_publicacion IS DISTINCT FROM NEW.fecha_publicacion)
    EXECUTE FUNCTION fn_recurso_en_uso_de_subtipo() ^^^

DROP TRIGGER IF EXISTS trg_material_bibliografico_fuente_ia ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_fuente_ia BEFORE UPDATE ON material_bibliografico
    FOR EACH ROW WHEN (OLD.publicado_en IS DISTINCT FROM NEW.publicado_en
                    OR OLD.identificador IS DISTINCT FROM NEW.identificador
                    OR OLD.fecha_publicacion IS DISTINCT FROM NEW.fecha_publicacion)
    EXECUTE FUNCTION fn_documento_bloqueado_por_ia('modificar') ^^^

-- No se admiten dos materiales bibliográficos vigentes con el mismo identificador (RecursoService.validarIdentificadorNoRepetido), salvo el ISSN.

-- fn_material_bibliografico_identificador(): disparador de los materiales bibliográficos. Rechaza un identificador que ya tiene otro material bibliográfico vigente.
CREATE OR REPLACE FUNCTION fn_material_bibliografico_identificador() RETURNS trigger AS $$
BEGIN
    -- El ISSN es de la revista y no de la obra: lo comparten todos sus artículos
    IF NEW.identificador ~* '^\d{4}-\d{3}[\dX]$' THEN
        RETURN NEW;
    END IF;
    IF EXISTS (SELECT 1 FROM material_bibliografico b JOIN documento d ON d.id_documento = b.id_documento
                                                      JOIN recurso r ON r.id_recurso = d.id_recurso
               WHERE r.baja = false AND b.id_documento <> NEW.id_documento
                 AND upper(replace(replace(b.identificador, '-', ''), ' ', '')) = upper(replace(replace(NEW.identificador, '-', ''), ' ', ''))) THEN
        RAISE EXCEPTION 'Error! Ya existe una bibliografía registrada con ese identificador.'
            USING ERRCODE = 'unique_violation', CONSTRAINT = TG_NAME;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql ^^^

DROP TRIGGER IF EXISTS trg_material_bibliografico_identificador ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_identificador BEFORE INSERT OR UPDATE OF identificador ON material_bibliografico
    FOR EACH ROW EXECUTE FUNCTION fn_material_bibliografico_identificador() ^^^

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_material_bibliografico_pertenencia ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_pertenencia BEFORE UPDATE ON material_bibliografico
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_documento') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_material_bibliografico_sin_borrado ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_sin_borrado BEFORE DELETE ON material_bibliografico
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^

-- ----------------------------------------------------------------------
-- Auditoría
-- ----------------------------------------------------------------------

-- Registra quién creó, modificó o eliminó cada registro (solo cuando lo hace una persona con sesión iniciada).
DROP TRIGGER IF EXISTS trg_material_bibliografico_auditoria ON material_bibliografico ^^^
CREATE TRIGGER trg_material_bibliografico_auditoria AFTER INSERT OR UPDATE OR DELETE ON material_bibliografico
    FOR EACH ROW EXECUTE FUNCTION fn_auditar('MaterialBibliografico') ^^^
