package com.app.idoneos.repository;

import com.app.idoneos.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoReporteRepository extends JpaRepository<TipoReporte, Integer> {

    /** CU-87 / CU-88: Buscar tipo de reporte por nombre para persistencia del historial. */
    Optional<TipoReporte> findByNombre(String nombre);
}
