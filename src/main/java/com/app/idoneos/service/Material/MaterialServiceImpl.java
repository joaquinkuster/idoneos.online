package com.app.idoneos.service.Material;

import com.app.idoneos.model.Material;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public Material guardar(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public Optional<Material> buscarPorId(Integer id) {
        return materialRepository.findById(id).filter(m -> !m.getBaja());
    }

    @Override
    public List<Material> obtenerTodo() {
        return materialRepository.findAll();
    }

    @Override
    public List<Material> obtenerPublicadosPorUnidad(Unidad unidad) {
        return materialRepository.findByUnidadAndBajaFalseAndPublicadoTrue(unidad);
    }

    @Override
    public List<Material> obtenerTodosPorUnidad(Unidad unidad) {
        return materialRepository.findByUnidadAndBajaFalse(unidad);
    }

    @Override
    public Material modificar(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public void borrar(Material material) {
        material.setBaja(true);
        materialRepository.save(material);
    }

    @Override
    public boolean existePorId(Integer id) {
        return materialRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
