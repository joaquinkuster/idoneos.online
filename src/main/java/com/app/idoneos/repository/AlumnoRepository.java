package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    /** CU-89 — Consultar estadísticas: alumnos activos (usuario no dado de baja). */
    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.usuario.baja = false")
    long contarAlumnosActivos();
}

