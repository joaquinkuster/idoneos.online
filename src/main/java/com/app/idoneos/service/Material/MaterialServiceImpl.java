package com.app.idoneos.service.Material;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Material;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de materiales de estudio de unidades (CU-25 a CU-28).
 */
@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {

    @Autowired private MaterialRepository materialRepository;

    /**
     * CU-25 — Buscar material por ID.
     */
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
        return materialRepository.findByUnidadAndBajaFalseAndPublicadoTrue(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerTodosPorUnidad(Unidad unidad) {
        return materialRepository.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * CU-26 — Subir material de estudio.
     * Reglas de negocio:
     * - Título obligatorio (Excepción CU-26, paso 4).
     * - Unidad temática asociada obligatoria.
     * - Tipo de material obligatorio (Excepción CU-26, paso 5).
     */
    @Override
    public Material guardar(Material material) {
        if (material.getTitulo() == null || material.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-26 Excepción paso 4: El título del material es obligatorio.");
        }
        if (material.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-26 Excepción paso 4: La unidad temática asociada es obligatoria.");
        }
        if (material.getTipoMaterial() == null) {
            throw new ExcepcionValidacion("CU-26 Excepción paso 5: El tipo de material es obligatorio.");
        }

        material.setBaja(false);
        material.setFechaCreacion(LocalDateTime.now());
        return materialRepository.save(material);
    }

    /**
     * CU-27 — Modificar material de estudio.
     */
    @Override
    public Material modificar(Material material) {
        Material existente = materialRepository.findById(material.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Material", "id", material.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-27 Precondición: No se puede modificar un material dado de baja.");
        }

        if (material.getTitulo() == null || material.getTitulo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-27 Excepción paso 4: El título del material no puede quedar vacío.");
        }

        existente.setTitulo(material.getTitulo().trim());
        existente.setContenido(material.getContenido());
        existente.setRutaArchivo(material.getRutaArchivo());
        existente.setPublicado(material.getPublicado());
        if (material.getTipoMaterial() != null) {
            existente.setTipoMaterial(material.getTipoMaterial());
        }
        return materialRepository.save(existente);
    }

    /**
     * CU-28 — Eliminar material (Baja Lógica).
     */
    @Override
    public void borrar(Material material) {
        darDeBajaMaterial(material.getId());
    }

    public void darDeBajaMaterial(int materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Material", "id", materialId));

        if (material.getBaja()) {
            throw new ExcepcionValidacion("CU-28 Excepción: El material ya se encuentra dado de baja.");
        }

        material.setBaja(true);
        materialRepository.save(material);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return materialRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
