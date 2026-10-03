package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Auditoria;
import com.app.idoneos.modelo.DetalleAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link DetalleAuditoria} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link DetalleAuditoria}.
 */
@Repository
public interface DetalleAuditoriaRepositorio extends JpaRepository<DetalleAuditoria, Integer> {

    /**
     * Busca los registros de detalleAuditoria asociados a auditoria.
     *
     * @param auditoria el registro de Auditoria asociado
     * @return una lista de DetalleAuditoria asociados al Auditoria indicado
     */
    List<DetalleAuditoria> findByAuditoria(Auditoria auditoria);
}
