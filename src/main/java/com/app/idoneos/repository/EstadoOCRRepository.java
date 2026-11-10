package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoOCR;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoOCR. */
@Repository
public interface EstadoOCRRepository extends JpaRepository<EstadoOCR, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<EstadoOCR> findByNombre(String nombre);
}
