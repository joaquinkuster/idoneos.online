package com.app.idoneos.repository;

import com.app.idoneos.model.Nivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NivelRepository extends JpaRepository<Nivel, Integer> {
    Optional<Nivel> findByNombre(String nombre);
}
