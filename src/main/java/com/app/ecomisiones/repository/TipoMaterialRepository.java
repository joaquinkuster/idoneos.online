package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.TipoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMaterialRepository extends JpaRepository<TipoMaterial, Integer> {
    Optional<TipoMaterial> findByNombre(String nombre);
}
