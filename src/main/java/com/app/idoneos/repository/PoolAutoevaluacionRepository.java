package com.app.idoneos.repository;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.PoolAutoevaluacion;
import com.app.idoneos.model.PoolAutoevaluacionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoolAutoevaluacionRepository extends JpaRepository<PoolAutoevaluacion, PoolAutoevaluacionId> {
    List<PoolAutoevaluacion> findByAutoevaluacion(Autoevaluacion autoevaluacion);
    List<PoolAutoevaluacion> findByPool(Pool pool);
}
