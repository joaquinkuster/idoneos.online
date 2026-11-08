package com.app.idoneos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.EstadoInscripcion;

/**
 * Repositorio para acceder y gestionar la entidad {@link EstadoInscripcion} en la base de datos.
 */
@Repository
public interface EstadoInscripcionRepository extends JpaRepository<EstadoInscripcion, Integer> {

    /**
     * Busca el estado de la inscripción por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoInscripcion> findByNombre(String nombre);
}
