package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.DetalleAuditoria;

/**
 * Repositorio para acceder y gestionar la entidad {@link DetalleAuditoria} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link DetalleAuditoria}.
 */
@Repository
public interface DetalleAuditoriaRepository extends JpaRepository<DetalleAuditoria, Integer> {

}
