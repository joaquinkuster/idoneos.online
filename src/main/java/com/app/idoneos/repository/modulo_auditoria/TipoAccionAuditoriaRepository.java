package com.app.idoneos.repository.modulo_auditoria;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAccionAuditoriaRepository extends JpaRepository<TipoAccionAuditoria, Integer> {
    Optional<TipoAccionAuditoria> findByNombre(String nombre);
}

