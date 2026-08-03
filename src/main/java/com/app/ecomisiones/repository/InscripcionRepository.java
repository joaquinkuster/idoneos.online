package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Inscripcion;
import com.app.ecomisiones.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    List<Inscripcion> findByAlumnoAndBajaFalse(Usuario alumno);

    Optional<Inscripcion> findByAlumnoAndCursoAndBajaFalse(Usuario alumno, Curso curso);

    boolean existsByAlumnoAndCursoAndBajaFalse(Usuario alumno, Curso curso);
}
