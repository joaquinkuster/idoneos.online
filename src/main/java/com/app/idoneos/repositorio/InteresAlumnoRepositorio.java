package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.InteresAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link InteresAlumno} en la base de datos.
 */
@Repository
public interface InteresAlumnoRepositorio extends JpaRepository<InteresAlumno, Integer> {
}
