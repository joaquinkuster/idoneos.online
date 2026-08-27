package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UnidadServiceImpl implements UnidadService {

    @Autowired private UnidadRepository unidadRepository;

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

    @Override
    public Unidad guardar(Unidad unidad) {
        if (unidad.getTitulo() == null || unidad.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-20 Excepción: El título de la unidad es obligatorio.");
        }

        unidad.setBaja(false);
        unidad.setFechaCreacion(LocalDateTime.now());
        return unidadRepository.save(unidad);
    }

    @Override
    public Unidad modificar(Unidad unidad) {
        Unidad existente = unidadRepository.findById(unidad.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Unidad", "id", unidad.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-21 Precondición: No se puede modificar una unidad dada de baja.");
        }
        if (unidad.getTitulo() == null || unidad.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-21 Excepción: El título de la unidad no puede estar vacío.");
        }

        existente.setTitulo(unidad.getTitulo().trim());
        existente.setDescripcion(unidad.getDescripcion());
        existente.setContenido(unidad.getContenido());
        existente.setUltimaModificacion(LocalDateTime.now());
        return unidadRepository.save(existente);
    }

    @Override
    public void borrar(Unidad unidad) {
        darDeBaja(unidad.getId());
    }

    @Override
    public void darDeBaja(Integer id) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Unidad", "id", id));
        unidad.setBaja(true);
        unidad.setUltimaModificacion(LocalDateTime.now());
        unidadRepository.save(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return unidadRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
