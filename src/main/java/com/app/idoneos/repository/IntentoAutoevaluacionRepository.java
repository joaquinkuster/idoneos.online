package com.app.idoneos.repository;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentoAutoevaluacionRepository extends JpaRepository<IntentoAutoevaluacion, Integer> {

    List<IntentoAutoevaluacion> findByAutoevaluacionOrderByFechaDesc(Autoevaluacion autoevaluacion);

    long countByAutoevaluacion(Autoevaluacion autoevaluacion);
}
