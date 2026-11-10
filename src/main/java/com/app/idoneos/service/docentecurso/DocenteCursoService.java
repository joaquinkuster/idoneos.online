package com.app.idoneos.service.docentecurso;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import java.util.List;

/** Servicio de la participación docente. */
public interface DocenteCursoService extends CrudService<DocenteCurso> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de docenteCurso asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de DocenteCurso asociados al Curso indicado
     */
    List<DocenteCurso> buscarPorCurso(Curso curso);

    /**
     * Busca el equipo docente de un curso, con los titulares primero.
     *
     * @param curso el curso
     * @return los docentes del curso, titulares antes que ayudantes
     */
    List<DocenteCurso> buscarEquipoPorCurso(Curso curso);

    /**
     * Busca los registros vigentes de docenteCurso asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de DocenteCurso asociados al Docente indicado
     */
    List<DocenteCurso> buscarPorDocente(Docente docente);

}
