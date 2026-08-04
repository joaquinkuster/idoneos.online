package com.app.idoneos.repository;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoevaluacionRepository extends JpaRepository<Autoevaluacion, Integer> {
    List<Autoevaluacion> findByPoolsContainingAndBajaFalse(Pool pool);
}
