package com.app.idoneos.repository;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findByBajaFalseAndPublicadoTrue();

    List<Curso> findByBajaFalse();

    List<Curso> findByCategoriaAndBajaFalseAndPublicadoTrue(Categoria categoria);

    List<Curso> findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(String query);

    /**
     * Cursos de un docente (titular o supervisor) via DocenteCurso.
     */
    @Query("SELECT dc.curso FROM DocenteCurso dc WHERE dc.docente.id = :docenteId AND dc.curso.baja = false")
    List<Curso> findByDocenteId(int docenteId);

    /**
     * Cursos donde el docente es titular (esSupervisor = false).
     */
    @Query("SELECT dc.curso FROM DocenteCurso dc WHERE dc.docente.id = :docenteId AND dc.esSupervisor = false AND dc.curso.baja = false")
    List<Curso> findByDocenteTitularId(int docenteId);
}
