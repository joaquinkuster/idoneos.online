package com.app.idoneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Pool;
import com.app.idoneos.model.PoolFuente;

/** Repositorio de PoolFuente. */
@Repository
public interface PoolFuenteRepository extends JpaRepository<PoolFuente, PoolFuente.Clave> {

    /** Busca las fuentes de un banco de preguntas. */
    List<PoolFuente> findByPool(Pool pool);
}
