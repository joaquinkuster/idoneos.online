package com.app.idoneos.repository;

import com.app.idoneos.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoReporteRepository extends JpaRepository<TipoReporte, Integer> {
}
