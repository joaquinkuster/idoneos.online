-- RolUsuario: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_rol_usuario_pertenencia ON rol_usuario ^^^
CREATE TRIGGER trg_rol_usuario_pertenencia BEFORE UPDATE ON rol_usuario
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_rol', 'id_usuario') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_rol_usuario_sin_borrado ON rol_usuario ^^^
CREATE TRIGGER trg_rol_usuario_sin_borrado BEFORE DELETE ON rol_usuario
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
