package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Grabacion;

/**
 * Repositorio para acceder y gestionar la entidad {@link Grabacion} en la base de datos.
 */
@Repository
public interface GrabacionRepository extends JpaRepository<Grabacion, Integer> {
}
