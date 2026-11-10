package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Pregunta;

/** Repositorio de Pregunta. */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Pregunta> findByBajaFalse();

}
