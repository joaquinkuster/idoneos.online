package com.app.idoneos.repository;

import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Integer> {
    Optional<Pool> findByUnidadAndBajaFalse(Unidad unidad);
}
