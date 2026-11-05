package com.app.idoneos.service.docente;

import com.app.idoneos.service.CrudService;

import java.util.List;

import com.app.idoneos.model.Docente;

/**
 * Servicio para gestionar las operaciones relacionadas con el docente.
 */
public interface DocenteService extends CrudService<Docente> {

    // ---------------------------------------------------------------- Lecturas





    /**
     * Busca los docentes activos y habilitados, que pueden participar en cursos.
     *
     * @return una lista de docentes habilitados
     */
    List<Docente> buscarHabilitados();
}
