package com.app.idoneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.InteresAlumno;

/**
 * Repositorio para acceder y gestionar la entidad {@link InteresAlumno} en la base de datos.
 */
@Repository
public interface InteresAlumnoRepository extends JpaRepository<InteresAlumno, Integer> {

    /**
     * Busca los intereses de un alumno.
     *
     * @param alumno El alumno.
     * @return Los intereses del alumno.
     */
    List<InteresAlumno> findByAlumno(Alumno alumno);

    /**
     * Borra todos los intereses de un alumno.
     *
     * @param alumno El alumno.
     */
    void deleteByAlumno(Alumno alumno);
}
