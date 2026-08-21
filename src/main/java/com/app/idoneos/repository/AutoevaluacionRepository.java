package com.app.idoneos.repository;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoevaluacionRepository extends JpaRepository<Autoevaluacion, Integer> {

    @Query("SELECT DISTINCT pa.autoevaluacion FROM PoolAutoevaluacion pa " +
           "WHERE pa.pool = :pool AND pa.autoevaluacion.baja = false")
    List<Autoevaluacion> findByPoolsContainingAndBajaFalse(@Param("pool") Pool pool);

    /** Autoevaluaciones de un curso (para panel docente) — CU-46. */
    @Query("SELECT DISTINCT a FROM Autoevaluacion a JOIN Cronograma c ON c.unidad = a.unidad " +
           "WHERE c.programa.curso = :curso AND a.baja = false")
    List<Autoevaluacion> findByCurso(@Param("curso") Curso curso);

    List<Autoevaluacion> findByUnidadAndBajaFalse(com.app.idoneos.model.Unidad unidad);
}
