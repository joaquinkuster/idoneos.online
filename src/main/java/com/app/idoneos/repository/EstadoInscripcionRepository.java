package com.app.idoneos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.EstadoInscripcion;

/** Repositorio de EstadoInscripcion. */
@Repository
public interface EstadoInscripcionRepository extends JpaRepository<EstadoInscripcion, Integer> {

    /** Busca el estado de la inscripción por su atributo único 'nombre'. */
    Optional<EstadoInscripcion> findByNombre(String nombre);
}
