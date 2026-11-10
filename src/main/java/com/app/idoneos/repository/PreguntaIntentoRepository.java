package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.PreguntaIntento;

/** Repositorio de PreguntaIntento. */
@Repository
public interface PreguntaIntentoRepository extends JpaRepository<PreguntaIntento, Integer> {

}
