package com.app.idoneos.repository;

import com.app.idoneos.model.TipoAccionAuditoria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de TipoAccionAuditoria. */
@Repository
public interface TipoAccionAuditoriaRepository extends JpaRepository<TipoAccionAuditoria, Integer> {

    /** Busca el tipo de acción auditada por su atributo único 'nombre'. */
    Optional<TipoAccionAuditoria> findByNombre(String nombre);
}
