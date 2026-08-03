package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Pool;
import com.app.ecomisiones.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Integer> {
    Optional<Pool> findByUnidadAndBajaFalse(Unidad unidad);
}
