package com.app.idoneos.repository;

import com.app.idoneos.model.Material;
import com.app.idoneos.model.TipoMaterial;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {

    List<Material> findByUnidadAndBajaFalseAndOcultoFalse(Unidad unidad);

    List<Material> findByUnidadAndBajaFalse(Unidad unidad);
    List<Material> findByUnidad(Unidad unidad);

    @Query("SELECT m FROM Material m WHERE m.tipoMaterial = :tipo AND m.baja = false")
    List<Material> findByTipoAndBajaFalse(@Param("tipo") TipoMaterial tipo);
}
