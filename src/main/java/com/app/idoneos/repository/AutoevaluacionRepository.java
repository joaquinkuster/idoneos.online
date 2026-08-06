package com.app.idoneos.repository;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoevaluacionRepository extends JpaRepository<Autoevaluacion, Integer> {

    List<Autoevaluacion> findByPoolsContainingAndBajaFalse(Pool pool);

    /** Autoevaluaciones de un curso (para panel docente) — CU-46. */
    @Query("SELECT a FROM Autoevaluacion a JOIN a.pools p WHERE p.unidad.curso = :curso AND a.baja = false")
    List<Autoevaluacion> findByCurso(Curso curso);
}
