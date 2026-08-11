package com.app.idoneos.service.Unidad;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de unidades temáticas (CU-19 a CU-24).
 */
@Service
@Transactional
public class UnidadServiceImpl implements UnidadService {

    @Autowired private UnidadRepository unidadRepository;

    /**
     * CU-19 — Buscar unidad por ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Unidad> buscarPorId(Integer id) {
        return unidadRepository.findById(id).filter(u -> !u.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Unidad> obtenerTodo() {
        return unidadRepository.findAll().stream().filter(u -> !u.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Unidad> obtenerPorCurso(Curso curso) {
        return unidadRepository.findByCursoAndBajaFalseOrderByNumeroOrdenAsc(curso);
    }

    @Override
    @Transactional(readOnly = true)
    public int contarUnidadesPorCurso(Curso curso) {
        return obtenerPorCurso(curso).size();
    }

    /**
     * CU-22 — Registrar unidad.
     * Reglas de negocio:
     * - Título de la unidad obligatorio (Excepción CU-22, paso 4).
     * - Programa o Curso asociado obligatorio.
     * - Número de orden mayor a cero (Excepción CU-22, paso 5).
     */
    @Override
    public Unidad guardar(Unidad unidad) {
        if (unidad.getTitulo() == null || unidad.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-22 Excepción paso 4: El título de la unidad es obligatorio.");
        }
        if (unidad.getNumeroOrden() <= 0) {
            throw new ExcepcionValidacion("CU-22 Excepción paso 5: El número de orden debe ser mayor a cero.");
        }

        unidad.setBaja(false);
        return unidadRepository.save(unidad);
    }

    /**
     * CU-23 — Modificar unidad.
     * CU-20 — Editar contenido de unidad.
     */
    @Override
    public Unidad modificar(Unidad unidad) {
        Unidad existente = unidadRepository.findById(unidad.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Unidad", "id", unidad.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-23 Precondición: No se puede modificar una unidad dada de baja.");
        }
        if (unidad.getTitulo() == null || unidad.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-23 Excepción paso 4: El título de la unidad no puede quedar vacío.");
        }
        if (unidad.getNumeroOrden() <= 0) {
            throw new ExcepcionValidacion("CU-23 Excepción paso 5: El número de orden debe ser mayor a cero.");
        }

        existente.setTitulo(unidad.getTitulo().trim());
        existente.setDescripcion(unidad.getDescripcion());
        existente.setNumeroOrden(unidad.getNumeroOrden());
        return unidadRepository.save(existente);
    }

    /**
     * CU-24 — Eliminar unidad (Baja Lógica).
     */
    @Override
    public void borrar(Unidad unidad) {
        darDeBajaUnidad(unidad.getId());
    }

    public void darDeBajaUnidad(int unidadId) {
        Unidad unidad = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Unidad", "id", unidadId));

        if (unidad.getBaja()) {
            throw new ExcepcionValidacion("CU-24 Excepción: La unidad ya se encuentra dada de baja.");
        }

        unidad.setBaja(true);
        unidadRepository.save(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return unidadRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
