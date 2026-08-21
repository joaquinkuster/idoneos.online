package com.app.idoneos.service.Categoria;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.repository.CategoriaRepository;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Implementación del servicio para la gestión del catálogo de categorías temáticas.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-07 — Buscar categoría: consulta de categorías activas por ID o nombre.
 *   CU-08 — Registrar categoría: alta de nueva categoría con validación de unicidad de nombre.
 *   CU-09 — Modificar categoría: edición de nombre y descripción.
 *   CU-10 — Dar de baja categoría: baja lógica con validación de cursos activos asociados.
 */
@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService, CrudService<Categoria> {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private CursoRepository cursoRepository;

    /**
     * CU-06 — Buscar categoría por identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id).filter(c -> !c.esInactivo());
    }

    /**
     * CU-06 — Obtener todas las categorías activas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodo() {
        return categoriaRepository.findByBajaFalse();
    }

    /**
     * CU-07 — Registrar categoría.
     * 
     * Reglas de negocio:
     * - Nombre obligatorio (Excepción CU-07, paso 4).
     * - Unicidad del nombre entre categorías activas (Excepción CU-07, paso 5).
     * - Registro en estado activo (baja = false) con fecha de creación.
     */
    @Override
    public Categoria guardar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-07 Excepción paso 4: El nombre de la categoría es obligatorio.");
        }

        String nombreLimpio = categoria.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndBajaFalse(nombreLimpio)) {
            throw new ExcepcionValidacion("CU-07 Excepción paso 5: Ya existe una categoría activa registrada con el nombre '" + nombreLimpio + "'.");
        }

        categoria.setNombre(nombreLimpio);
        categoria.setBaja(false);
        categoria.setFechaCreacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    /**
     * CU-08 — Modificar categoría.
     * 
     * Reglas de negocio:
     * - Verificación de existencia y estado activo.
     * - Nombre obligatorio (Excepción CU-08, paso 4).
     * - Evitar nombres duplicados entre categorías activas (Excepción CU-08, paso 5).
     */
    @Override
    public Categoria modificar(Categoria categoria) {
        Categoria existente = categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Categoría", "id", categoria.getId()));

        if (existente.esInactivo()) {
            throw new ExcepcionValidacion("CU-08 Precondición: No se puede modificar una categoría dada de baja.");
        }

        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-08 Excepción paso 4: El nombre de la categoría no puede quedar vacío.");
        }

        String nuevoNombre = categoria.getNombre().trim();
        if (!existente.getNombre().equalsIgnoreCase(nuevoNombre) && 
            categoriaRepository.existsByNombreIgnoreCaseAndBajaFalse(nuevoNombre)) {
            throw new ExcepcionValidacion("CU-08 Excepción paso 5: Ya existe otra categoría activa con el nombre '" + nuevoNombre + "'.");
        }

        existente.setNombre(nuevoNombre);
        existente.setDescripcion(categoria.getDescripcion());
        return categoriaRepository.save(existente);
    }

    /**
     * CU-09 — Eliminar categoría (Baja Lógica).
     * 
     * Reglas de negocio:
     * - Valida existencia de la categoría.
     * - Verifica si existen cursos activos asociados a la categoría.
     * - Si posee cursos activos asociados, impide la baja (Excepción CU-09, paso 5).
     * - Marca baja = true.
     */
    @Override
    public void borrar(Categoria categoria) {
        darDeBaja(categoria.getId());
    }

    @Override
    public void darDeBaja(Integer categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Categoría", "id", categoriaId));

        if (categoria.esInactivo()) {
            throw new ExcepcionValidacion("CU-09 Excepción: La categoría ya se encuentra dada de baja.");
        }

        List<Curso> cursosAsociados = cursoRepository.findByCategoriaAndBajaFalseAndPublicadoTrue(categoria);
        if (!cursosAsociados.isEmpty()) {
            throw new ExcepcionValidacion("CU-09 Excepción paso 5: No se puede dar de baja la categoría porque posee " + cursosAsociados.size() + " curso(s) activo(s) asociado(s).");
        }

        categoria.setBaja(true);
        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return categoriaRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
