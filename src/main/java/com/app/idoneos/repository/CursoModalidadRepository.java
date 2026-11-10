package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.CursoModalidad;

/** Repositorio de CursoModalidad. */
@Repository
public interface CursoModalidadRepository extends JpaRepository<CursoModalidad, Integer> {

    /** Busca los registros de cursoModalidad asociados a curso. */
    List<CursoModalidad> findByCurso(Curso curso);

}
