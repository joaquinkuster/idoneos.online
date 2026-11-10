package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de DocenteCurso. */
@Repository
public interface DocenteCursoRepository extends JpaRepository<DocenteCurso, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<DocenteCurso> findByBajaFalse();

    /** Busca los registros vigentes de docenteCurso asociados a curso. */
    List<DocenteCurso> findByCursoAndBajaFalse(Curso curso);

    /** Busca los registros vigentes de docenteCurso asociados a docente. */
    List<DocenteCurso> findByDocenteAndBajaFalse(Docente docente);

}
