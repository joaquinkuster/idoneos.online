package com.app.idoneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.InteresAlumno;

/** Repositorio de InteresAlumno. */
@Repository
public interface InteresAlumnoRepository extends JpaRepository<InteresAlumno, Integer> {

    /** Busca los intereses de un alumno. */
    List<InteresAlumno> findByAlumno(Alumno alumno);

    /** Borra todos los intereses de un alumno. */
    void deleteByAlumno(Alumno alumno);
}
