package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoClaseEnVivo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoClaseEnVivo. */
@Repository
public interface EstadoClaseEnVivoRepository extends JpaRepository<EstadoClaseEnVivo, Integer> {

    /** Busca el estado de la clase en vivo por su atributo único 'nombre'. */
    Optional<EstadoClaseEnVivo> findByNombre(String nombre);
}
