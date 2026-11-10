package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Programa. */
@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Programa> findByBajaFalse();

    /** Busca los registros vigentes de programa asociados a curso. */
    List<Programa> findByCursoAndBajaFalse(Curso curso);
}
