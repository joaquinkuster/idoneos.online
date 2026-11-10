package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RespuestaIntento;

/** Repositorio de RespuestaIntento. */
@Repository
public interface RespuestaIntentoRepository extends JpaRepository<RespuestaIntento, Integer> {

}
