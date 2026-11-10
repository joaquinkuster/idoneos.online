package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoEnvio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoEnvio. */
@Repository
public interface EstadoEnvioRepository extends JpaRepository<EstadoEnvio, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<EstadoEnvio> findByNombre(String nombre);
}
