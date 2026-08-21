package com.app.idoneos.repository.modulo_auditoria;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    List<Auditoria> findAllByOrderByFechaHoraDesc();
    List<Auditoria> findByEntidadAfectadaContainingIgnoreCaseOrderByFechaHoraDesc(String entidadAfectada);
}


