package com.app.idoneos.service.detalleauditoria;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.DetalleAuditoria;

/** Servicio del detalle de auditoría. */
public interface DetalleAuditoriaService extends CrudService<DetalleAuditoria, Integer> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Obtiene los campos afectados por una acción auditada.
     *
     * @param auditoria la acción auditada
     * @return los detalles, en orden alfabético de campo
     */
    java.util.List<DetalleAuditoria> buscarPorAuditoria(com.app.idoneos.model.Auditoria auditoria);

    /**
     * Obtiene, en una sola consulta, los campos afectados por varias acciones auditadas.
     *
     * @param auditorias las acciones auditadas
     * @return los detalles agrupados por identificador de auditoría, en orden alfabético de campo
     */
    java.util.Map<Integer, java.util.List<DetalleAuditoria>> buscarPorAuditorias(java.util.Collection<com.app.idoneos.model.Auditoria> auditorias);

}
