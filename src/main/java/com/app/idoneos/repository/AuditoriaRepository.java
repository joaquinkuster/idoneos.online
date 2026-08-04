package com.app.idoneos.repository;

import com.app.idoneos.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    List<Auditoria> findAllByOrderByFechaHoraDesc();
    List<Auditoria> findByEntidadAfectadaContainingIgnoreCaseOrderByFechaHoraDesc(String entidadAfectada);
}

