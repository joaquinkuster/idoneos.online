package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.IntentoAutoevaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentoAutoevaluacionRepository extends JpaRepository<IntentoAutoevaluacion, Integer> {
}
