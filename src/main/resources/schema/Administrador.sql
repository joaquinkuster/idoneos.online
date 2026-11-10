-- Administrador: reglas de la base de datos de esta entidad.
-- Cada sentencia termina con ^^^ porque los cuerpos de las funciones llevan punto y coma adentro. Se puede ejecutar más de una vez.

-- ----------------------------------------------------------------------
-- Columnas que no cambian
-- ----------------------------------------------------------------------

-- A quién pertenece este registro no cambia: solo cambian sus demás datos.
DROP TRIGGER IF EXISTS trg_administrador_pertenencia ON administrador ^^^
CREATE TRIGGER trg_administrador_pertenencia BEFORE UPDATE ON administrador
    FOR EACH ROW EXECUTE FUNCTION fn_columnas_inmutables('id_usuario') ^^^

-- ----------------------------------------------------------------------
-- Sin borrado
-- ----------------------------------------------------------------------

-- Las bajas son lógicas: el registro no se borra.
DROP TRIGGER IF EXISTS trg_administrador_sin_borrado ON administrador ^^^
CREATE TRIGGER trg_administrador_sin_borrado BEFORE DELETE ON administrador
    FOR EACH ROW EXECUTE FUNCTION fn_sin_borrado() ^^^
