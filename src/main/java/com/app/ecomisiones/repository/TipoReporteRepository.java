package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoReporteRepository extends JpaRepository<TipoReporte, Integer> {
}
