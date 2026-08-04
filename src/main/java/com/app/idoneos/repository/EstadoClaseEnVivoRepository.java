package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoClaseEnVivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoClaseEnVivoRepository extends JpaRepository<EstadoClaseEnVivo, Integer> {
    Optional<EstadoClaseEnVivo> findByNombre(String nombre);
}
