package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.IntegranteEquipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de IntegranteEquipo. */
@Repository
public interface IntegranteEquipoRepository extends JpaRepository<IntegranteEquipo, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<IntegranteEquipo> findByBajaFalse();

    /** Busca los registros vigentes de integranteEquipo asociados a curso. */
    List<IntegranteEquipo> findByCursoAndBajaFalse(Curso curso);

    /** Busca los registros vigentes de integranteEquipo asociados a docente. */
    List<IntegranteEquipo> findByDocenteAndBajaFalse(Docente docente);

}
