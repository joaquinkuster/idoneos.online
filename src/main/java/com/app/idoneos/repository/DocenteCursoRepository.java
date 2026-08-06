package com.app.idoneos.repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteCursoRepository extends JpaRepository<DocenteCurso, Integer> {

    /** Todos los registros DocenteCurso de un curso (titular + supervisor). */
    List<DocenteCurso> findByCurso(Curso curso);

    /** Todos los registros DocenteCurso de un docente. */
    List<DocenteCurso> findByDocente(Docente docente);

    /** Registro de un docente específico en un curso específico. */
    Optional<DocenteCurso> findByDocenteAndCurso(Docente docente, Curso curso);

    /** Eliminar todas las asignaciones de un curso (para reasignar docentes). */
    void deleteByCurso(Curso curso);

    /** Cursos donde el docente es titular. */
    List<DocenteCurso> findByDocenteAndEsSupervisorFalse(Docente docente);

    /** Verificar si un docente tiene algún curso publicado como titular. */
    boolean existsByDocenteAndEsSupervisorFalseAndCurso_PublicadoTrue(Docente docente);
}
