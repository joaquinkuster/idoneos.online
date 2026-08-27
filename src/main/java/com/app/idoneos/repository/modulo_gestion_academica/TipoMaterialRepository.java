package com.app.idoneos.repository.modulo_gestion_academica;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMaterialRepository extends JpaRepository<TipoMaterial, Integer> {
    Optional<TipoMaterial> findByNombre(String nombre);
}

