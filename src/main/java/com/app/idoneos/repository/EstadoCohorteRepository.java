package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoCohorte;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoCohorte. */
@Repository
public interface EstadoCohorteRepository extends JpaRepository<EstadoCohorte, Integer> {

    /** Busca el estado de la cohorte por su atributo único 'nombre'. */
    Optional<EstadoCohorte> findByNombre(String nombre);
}
