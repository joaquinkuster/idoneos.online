package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.TipoAccionAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoAccionAuditoriaRepository extends JpaRepository<TipoAccionAuditoria, Integer> {
}
