package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Tema;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Tema. */
@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Tema> findByBajaFalse();

    /** Busca los registros vigentes de tema asociados a curso. */
    List<Tema> findByCursoAndBajaFalse(Curso curso);
}
