package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.MaterialLectura;

/**
 * Repositorio para acceder y gestionar la entidad {@link MaterialLectura} en la base de datos.
 */
@Repository
public interface MaterialLecturaRepository extends JpaRepository<MaterialLectura, Integer> {
}
