package com.app.idoneos.service.modulo_cursos;

import com.app.idoneos.model.*;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Contrato de Servicio para la gestión de categorías temáticas de cursos.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-07 — Buscar categoría
 *   CU-08 — Registrar categoría
 *   CU-09 — Modificar categoría
 *   CU-10 — Dar de baja categoría
 */
public interface CategoriaService {

    Categoria guardar(Categoria categoria);

    Categoria modificar(Categoria categoria);

    void darDeBaja(Integer id);

    Optional<Categoria> buscarPorId(int id);

    List<Categoria> obtenerTodo();

    List<Categoria> buscarPorNombre(String nombre);

    List<Categoria> buscarCategoriasConFiltros(String nombre, Boolean baja);
}
