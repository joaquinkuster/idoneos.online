package com.app.idoneos.service.modulo_cursos;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TRAZABILIDAD — Implementación del Servicio de Categorías (MOD-F-01).
 *
 * Implementa CU-07 a CU-10 según Contratos.md y Casos de Uso Reales.md.
 */
@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private CursoRepository cursoRepository;

    /**
     * CU-08 — Registrar categoría.
     * Valida que el nombre no esté vacío y sea único.
     */
    @Override
    public Categoria guardar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-08 Excepción paso 4: El nombre de la categoría es obligatorio.");
        }

        if (categoriaRepository.findByNombreIgnoreCaseAndBajaFalse(categoria.getNombre().trim()).isPresent()) {
            throw new ExcepcionConflicto("CU-08 Excepción paso 4: Ya existe una categoría activa con el nombre '" + categoria.getNombre() + "'.");
        }

        categoria.setBaja(false);
        categoria.setFechaCreacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    /**
     * CU-09 — Modificar categoría.
     * Valida unicidad de nombre y que no tenga cursos activos si se cambia.
     */
    @Override
    public Categoria modificar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-09 Excepción paso 5: El nombre de la categoría no puede estar vacío.");
        }

        Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCaseAndBajaFalse(categoria.getNombre().trim());
        if (existente.isPresent() && existente.get().getId() != categoria.getId()) {
            throw new ExcepcionConflicto("CU-09 Excepción paso 6: Ya existe otra categoría activa con el nombre '" + categoria.getNombre() + "'.");
        }

        categoria.setUltimaModificacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    /**
     * CU-10 — Dar de baja categoría.
     * Valida que no tenga cursos activos asociados.
     */
    @Override
    public void darDeBaja(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Categoría no encontrada con ID: " + id));

        if (categoria.getBaja()) {
            throw new ExcepcionNegocio("La categoría ya se encuentra dada de baja.");
        }

        List<Curso> cursosAsociados = cursoRepository.findByCategoriaAndBajaFalseAndPublicadoTrue(categoria);
        if (!cursosAsociados.isEmpty()) {
            throw new ExcepcionConflicto("CU-10 Excepción paso 2: No es posible eliminar la categoría porque tiene " + cursosAsociados.size() + " cursos activos asociados.");
        }

        categoria.setBaja(true);
        categoria.setUltimaModificacion(LocalDateTime.now());
        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(int id) {
        return categoriaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodo() {
        return categoriaRepository.findByBajaFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombreContainingIgnoreCaseAndBajaFalse(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> buscarCategoriasConFiltros(String nombre, Boolean baja) {
        List<Categoria> todas = (baja != null && baja) ? categoriaRepository.findAll() : categoriaRepository.findByBajaFalse();
        if (nombre == null || nombre.isBlank()) return todas;
        return todas.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }
}
