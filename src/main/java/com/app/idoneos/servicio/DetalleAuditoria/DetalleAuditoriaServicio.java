package com.app.idoneos.servicio.DetalleAuditoria;

import java.util.List;
import com.app.idoneos.modelo.Auditoria;
import com.app.idoneos.modelo.DetalleAuditoria;

/**
 * Servicio para gestionar las operaciones relacionadas con el detalle de auditoría.
 */
public interface DetalleAuditoriaServicio {

    /**
     * Busca los registros de detalleAuditoria asociados a auditoria.
     *
     * @param auditoria el registro de Auditoria asociado
     * @return una lista de DetalleAuditoria asociados al Auditoria indicado
     */
    List<DetalleAuditoria> buscarPorAuditoria(Auditoria auditoria);
}
