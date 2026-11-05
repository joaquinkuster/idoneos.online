package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RespuestaIntento;

/**
 * Repositorio para acceder y gestionar la entidad {@link RespuestaIntento} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link RespuestaIntento}.
 */
@Repository
public interface RespuestaIntentoRepository extends JpaRepository<RespuestaIntento, Integer> {



}
