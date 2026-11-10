package com.app.idoneos.service.titulodocente;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TituloDocente;
import java.util.List;

/** Servicio del título del docente. */
public interface TituloDocenteService extends CrudService<TituloDocente> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de tituloDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de TituloDocente asociados al Docente indicado
     */
    List<TituloDocente> buscarPorDocente(Docente docente);
}
