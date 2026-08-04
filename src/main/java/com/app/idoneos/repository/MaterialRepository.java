package com.app.idoneos.repository;

import com.app.idoneos.model.Material;
import com.app.idoneos.model.TipoMaterial;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {

    List<Material> findByUnidadAndBajaFalseAndPublicadoTrue(Unidad unidad);

    List<Material> findByUnidadAndBajaFalse(Unidad unidad);

    List<Material> findByTipoAndBajaFalse(TipoMaterial tipo);
}
