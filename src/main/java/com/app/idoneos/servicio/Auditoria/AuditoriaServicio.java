package com.app.idoneos.servicio.Auditoria;

import java.util.List;
import com.app.idoneos.modelo.Auditoria;
import com.app.idoneos.modelo.TipoAccionAuditoria;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con la auditoría.
 */
public interface AuditoriaServicio {

    /**
     * Busca los registros de auditoria asociados a tipoAccionAuditoria.
     *
     * @param tipoAuditoria el registro de TipoAccionAuditoria asociado
     * @return una lista de Auditoria asociados al TipoAccionAuditoria indicado
     */
    List<Auditoria> buscarPorTipoAuditoria(TipoAccionAuditoria tipoAuditoria);

    /**
     * Busca los registros de auditoria asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de Auditoria asociados al Usuario indicado
     */
    List<Auditoria> buscarPorUsuario(Usuario usuario);
}
