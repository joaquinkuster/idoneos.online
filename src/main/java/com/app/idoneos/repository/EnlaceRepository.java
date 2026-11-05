package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Enlace;

/**
 * Repositorio para acceder y gestionar la entidad {@link Enlace} en la base de datos.
 */
@Repository
public interface EnlaceRepository extends JpaRepository<Enlace, Integer> {
}
