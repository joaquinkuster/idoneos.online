package com.app.idoneos.repository;

import com.app.idoneos.model.TipoAccionAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAccionAuditoriaRepository extends JpaRepository<TipoAccionAuditoria, Integer> {
    Optional<TipoAccionAuditoria> findByNombre(String nombre);
}
