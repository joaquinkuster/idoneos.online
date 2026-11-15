package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoGeneracionIA;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoGeneracionIA. */
@Repository
public interface EstadoGeneracionIARepository extends JpaRepository<EstadoGeneracionIA, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<EstadoGeneracionIA> findByNombre(String nombre);
}
