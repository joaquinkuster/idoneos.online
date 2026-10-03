package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Auditoria;
import com.app.idoneos.modelo.TipoAccionAuditoria;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Auditoria} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Auditoria}.
 */
@Repository
public interface AuditoriaRepositorio extends JpaRepository<Auditoria, Integer> {

    /**
     * Busca los registros de auditoria asociados a tipoAccionAuditoria.
     *
     * @param tipoAuditoria el registro de TipoAccionAuditoria asociado
     * @return una lista de Auditoria asociados al TipoAccionAuditoria indicado
     */
    List<Auditoria> findByTipoAuditoria(TipoAccionAuditoria tipoAuditoria);

    /**
     * Busca los registros de auditoria asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de Auditoria asociados al Usuario indicado
     */
    List<Auditoria> findByUsuario(Usuario usuario);
}
