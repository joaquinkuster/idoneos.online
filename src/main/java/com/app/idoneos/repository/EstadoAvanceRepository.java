package com.app.idoneos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.EstadoAvance;

/**
 * Repositorio para acceder y gestionar la entidad {@link EstadoAvance} en la base de datos.
 */
@Repository
public interface EstadoAvanceRepository extends JpaRepository<EstadoAvance, Integer> {

    /**
     * Busca el estado del avance por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoAvance> findByNombre(String nombre);
}
