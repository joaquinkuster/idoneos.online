package com.app.idoneos.repository.modulo_evaluaciones;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentoAutoevaluacionRepository extends JpaRepository<IntentoAutoevaluacion, Integer> {

    List<IntentoAutoevaluacion> findByAutoevaluacionOrderByFechaEntregaDesc(Autoevaluacion autoevaluacion);

    default List<IntentoAutoevaluacion> findByAutoevaluacionOrderByFechaDesc(Autoevaluacion autoevaluacion) {
        return findByAutoevaluacionOrderByFechaEntregaDesc(autoevaluacion);
    }

    long countByAutoevaluacion(Autoevaluacion autoevaluacion);
}

