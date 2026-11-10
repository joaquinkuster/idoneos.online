package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoGeneracion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoGeneracion. */
@Repository
public interface EstadoGeneracionRepository extends JpaRepository<EstadoGeneracion, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<EstadoGeneracion> findByNombre(String nombre);
}
