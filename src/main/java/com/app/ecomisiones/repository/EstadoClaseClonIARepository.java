package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.EstadoClaseClonIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoClaseClonIARepository extends JpaRepository<EstadoClaseClonIA, Integer> {
    Optional<EstadoClaseClonIA> findByNombre(String nombre);
}
