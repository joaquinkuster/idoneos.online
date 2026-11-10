package com.app.idoneos.service.reporte;

import com.app.idoneos.service.CrudService;

import java.util.List;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;

/** Servicio del reporte. */
public interface ReporteService extends CrudService<Reporte> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    List<Reporte> buscarPorCurso(Curso curso);
}
