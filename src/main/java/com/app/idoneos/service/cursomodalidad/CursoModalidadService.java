package com.app.idoneos.service.cursomodalidad;

import com.app.idoneos.service.CrudService;

import java.util.List;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.CursoModalidad;

/**
 * Servicio para gestionar las operaciones relacionadas con la relación entre curso y modalidad.
 */
public interface CursoModalidadService extends CrudService<CursoModalidad> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros de cursoModalidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de CursoModalidad asociados al Curso indicado
     */
    List<CursoModalidad> buscarPorCurso(Curso curso);


}
