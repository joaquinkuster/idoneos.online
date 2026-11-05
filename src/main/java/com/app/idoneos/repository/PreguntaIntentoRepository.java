package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.PreguntaIntento;

/**
 * Repositorio para acceder y gestionar la entidad {@link PreguntaIntento} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link PreguntaIntento}.
 */
@Repository
public interface PreguntaIntentoRepository extends JpaRepository<PreguntaIntento, Integer> {



}
