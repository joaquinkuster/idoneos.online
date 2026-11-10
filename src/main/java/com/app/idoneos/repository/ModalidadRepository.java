package com.app.idoneos.repository;

import com.app.idoneos.model.Modalidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Modalidad. */
@Repository
public interface ModalidadRepository extends JpaRepository<Modalidad, Integer> {

    /** Busca la modalidad por su atributo único 'nombre'. */
    Optional<Modalidad> findByNombre(String nombre);
}
