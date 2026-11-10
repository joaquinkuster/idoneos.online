package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.DetalleAuditoria;

/** Repositorio de DetalleAuditoria. */
@Repository
public interface DetalleAuditoriaRepository extends JpaRepository<DetalleAuditoria, Integer> {

}
