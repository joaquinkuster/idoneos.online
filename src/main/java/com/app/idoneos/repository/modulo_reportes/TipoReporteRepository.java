package com.app.idoneos.repository.modulo_reportes;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoReporteRepository extends JpaRepository<TipoReporte, Integer> {

    /** CU-87 / CU-88: Buscar tipo de reporte por nombre para persistencia del historial. */
    Optional<TipoReporte> findByNombre(String nombre);
}

