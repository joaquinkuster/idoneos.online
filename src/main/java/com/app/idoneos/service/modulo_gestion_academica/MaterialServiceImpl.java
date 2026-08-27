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
public class MaterialServiceImpl implements MaterialService {

    @Autowired private MaterialRepository materialRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Material> buscarPorId(Integer id) {
        return materialRepository.findById(id).filter(m -> !m.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerTodo() {
        return materialRepository.findAll().stream().filter(m -> !m.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerPublicadosPorUnidad(Unidad unidad) {
        return materialRepository.findByUnidadAndBajaFalseAndOcultoFalse(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerTodosPorUnidad(Unidad unidad) {
        return materialRepository.findByUnidadAndBajaFalse(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerPorUnidad(Unidad unidad) {
        return materialRepository.findByUnidadAndBajaFalse(unidad);
    }

    @Override
    public Material guardar(Material material) {
        if (material.getTitulo() == null || material.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-28 Excepción: El título del material es obligatorio.");
        }
        if (material.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-28 Excepción: La unidad temática asociada es obligatoria.");
        }
        if (material.getTipoMaterial() == null) {
            throw new ExcepcionValidacion("CU-28 Excepción: El tipo de material es obligatorio.");
        }

        material.setBaja(false);
        material.setFechaCreacion(LocalDateTime.now());
        return materialRepository.save(material);
    }

    @Override
    public Material modificar(Material material) {
        Material existente = materialRepository.findById(material.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Material", "id", material.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-29 Precondición: No se puede modificar un material dado de baja.");
        }

        if (material.getTitulo() == null || material.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-29 Excepción: El título del material no puede quedar vacío.");
        }

        existente.setTitulo(material.getTitulo().trim());
        existente.setContenido(material.getContenido());
        existente.setRutaArchivo(material.getRutaArchivo());
        existente.setOculto(material.getOculto());
        if (material.getTipoMaterial() != null) {
            existente.setTipoMaterial(material.getTipoMaterial());
        }
        existente.setUltimaModificacion(LocalDateTime.now());
        return materialRepository.save(existente);
    }

    @Override
    public void borrar(Material material) {
        darDeBaja(material.getId());
    }

    @Override
    public void darDeBaja(Integer id) {
        darDeBajaMaterial(id);
    }

    @Override
    public void darDeBajaMaterial(int materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Material", "id", materialId));

        if (material.getBaja()) {
            throw new ExcepcionValidacion("El material ya se encuentra dado de baja.");
        }

        material.setBaja(true);
        material.setUltimaModificacion(LocalDateTime.now());
        materialRepository.save(material);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return materialRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
