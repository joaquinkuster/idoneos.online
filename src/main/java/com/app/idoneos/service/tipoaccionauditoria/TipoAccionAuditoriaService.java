package com.app.idoneos.service.tipoaccionauditoria;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.TipoAccionAuditoria;
import java.util.Optional;

/** Servicio del tipo de acción auditada. */
public interface TipoAccionAuditoriaService extends CrudService<TipoAccionAuditoria> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el tipo de acción auditada por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TipoAccionAuditoria> buscarPorNombre(String nombre);
}
