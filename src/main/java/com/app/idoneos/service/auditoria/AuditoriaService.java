package com.app.idoneos.service.auditoria;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.Auditoria;

/** Servicio de la auditoría. */
public interface AuditoriaService extends CrudService<Auditoria, Integer> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Obtiene todos los registros de auditoría, del más reciente al más antiguo.
     *
     * @return la lista de registros
     */
    java.util.List<Auditoria> buscarRecientes();

}
