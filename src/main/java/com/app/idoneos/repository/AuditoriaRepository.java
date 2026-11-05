package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Auditoria;

/**
 * Repositorio para acceder y gestionar la entidad {@link Auditoria} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Auditoria}.
 */
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {


}
