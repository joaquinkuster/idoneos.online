package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoClaseClonIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoClaseClonIARepository extends JpaRepository<EstadoClaseClonIA, Integer> {
    Optional<EstadoClaseClonIA> findByNombre(String nombre);
}
