package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Integer> {

    Optional<Pool> findByUnidadAndBajaFalse(Unidad unidad);

    /** Pools de un curso (vía sus unidades) — para panel docente (CU-46). */
    @Query("SELECT p FROM Pool p WHERE p.unidad.curso = :curso AND p.baja = false")
    List<Pool> findByCurso(Curso curso);
}
