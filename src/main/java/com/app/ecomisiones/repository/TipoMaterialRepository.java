package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.TipoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMaterialRepository extends JpaRepository<TipoMaterial, Integer> {
}
