package com.app.idoneos.repository.modulo_evaluaciones;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoolAutoevaluacionRepository extends JpaRepository<PoolAutoevaluacion, PoolAutoevaluacionId> {
    List<PoolAutoevaluacion> findByAutoevaluacion(Autoevaluacion autoevaluacion);
    List<PoolAutoevaluacion> findByPool(Pool pool);
}

