package com.app.idoneos.repository;

import com.app.idoneos.model.Nivel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Nivel. */
@Repository
public interface NivelRepository extends JpaRepository<Nivel, Integer> {

    /** Busca el nivel por su atributo único 'nombre'. */
    Optional<Nivel> findByNombre(String nombre);
}
