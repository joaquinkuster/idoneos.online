package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Sesion;

/**
 * Repositorio para acceder y gestionar la entidad {@link Sesion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Sesion}.
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {


}
