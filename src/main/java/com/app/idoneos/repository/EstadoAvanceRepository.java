package com.app.idoneos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.EstadoAvance;

/** Repositorio de EstadoAvance. */
@Repository
public interface EstadoAvanceRepository extends JpaRepository<EstadoAvance, Integer> {

    /** Busca el estado del avance por su atributo único 'nombre'. */
    Optional<EstadoAvance> findByNombre(String nombre);
}
