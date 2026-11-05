package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Administrador;

/**
 * Repositorio para acceder y gestionar la entidad {@link Administrador} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Administrador}.
 */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

}
