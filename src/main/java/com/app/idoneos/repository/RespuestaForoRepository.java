package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RespuestaForo;

/** Repositorio de RespuestaForo. */
@Repository
public interface RespuestaForoRepository extends JpaRepository<RespuestaForo, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<RespuestaForo> findByBajaFalse();

}
