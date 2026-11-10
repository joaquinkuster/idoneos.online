package com.app.idoneos.repository;

import com.app.idoneos.model.TipoReporte;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de TipoReporte. */
@Repository
public interface TipoReporteRepository extends JpaRepository<TipoReporte, Integer> {

    /** Busca el tipo de reporte por su atributo único 'nombre'. */
    Optional<TipoReporte> findByNombre(String nombre);
}
