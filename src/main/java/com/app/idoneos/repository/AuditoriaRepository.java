package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Auditoria;

/** Repositorio de Auditoria. */
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    /** Todos los registros, del más reciente al más antiguo. */
    java.util.List<Auditoria> findAllByOrderByFechaHoraDescIdAuditoriaDesc();

}
