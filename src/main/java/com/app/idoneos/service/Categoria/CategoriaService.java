package com.app.idoneos.service.Categoria;

import com.app.idoneos.model.Categoria;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Servicio para la gestión del catálogo de categorías temáticas de cursos.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-07 — Buscar categoría: consulta de categorías activas por ID o nombre.
 *   CU-08 — Registrar categoría: alta de nueva categoría con validación de unicidad de nombre.
 *   CU-09 — Modificar categoría: edición de nombre y descripción.
 *   CU-10 — Dar de baja categoría: baja lógica con validación de cursos activos asociados.
 */
public interface CategoriaService {

    Optional<Categoria> buscarPorId(Integer id);

    List<Categoria> obtenerTodo();

    Optional<Categoria> buscarPorNombre(String nombre);

    List<Categoria> buscarPorNombreContiene(String texto);

    Categoria guardar(Categoria categoria);

    Categoria modificar(Integer id, Categoria categoriaModificada);

    void darDeBaja(Integer id);
}
