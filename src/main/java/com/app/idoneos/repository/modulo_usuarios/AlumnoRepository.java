package com.app.idoneos.repository.modulo_usuarios;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    java.util.Optional<Alumno> findByUsuario(com.app.idoneos.model.Usuario usuario);

    /** CU-89 — Consultar estadísticas: alumnos activos (usuario no dado de baja). */
    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.usuario.baja = false")
    long contarAlumnosActivos();
}

