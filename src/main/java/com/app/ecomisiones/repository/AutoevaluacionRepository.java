package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Autoevaluacion;
import com.app.ecomisiones.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoevaluacionRepository extends JpaRepository<Autoevaluacion, Integer> {
    List<Autoevaluacion> findByPoolsContainingAndBajaFalse(Pool pool);
}
