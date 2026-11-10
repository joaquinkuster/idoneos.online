package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.OpcionRespuesta;

/** Repositorio de OpcionRespuesta. */
@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<OpcionRespuesta> findByBajaFalse();

}
