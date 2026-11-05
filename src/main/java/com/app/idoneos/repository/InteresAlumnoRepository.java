package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.InteresAlumno;

/**
 * Repositorio para acceder y gestionar la entidad {@link InteresAlumno} en la base de datos.
 */
@Repository
public interface InteresAlumnoRepository extends JpaRepository<InteresAlumno, Integer> {
}
