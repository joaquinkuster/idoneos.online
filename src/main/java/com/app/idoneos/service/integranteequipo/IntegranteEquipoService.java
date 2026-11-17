package com.app.idoneos.service.integranteequipo;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.IntegranteEquipo;
import java.util.List;

/** Servicio de la participación docente. */
public interface IntegranteEquipoService extends CrudService<IntegranteEquipo, Integer> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de integranteEquipo asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de IntegranteEquipo asociados al Curso indicado
     */
    List<IntegranteEquipo> buscarPorCurso(Curso curso);

    /**
     * Busca el equipo docente de un curso, con los titulares primero.
     *
     * @param curso el curso
     * @return los docentes del curso, titulares antes que ayudantes
     */
    List<IntegranteEquipo> buscarEquipoPorCurso(Curso curso);

    /**
     * Busca los registros vigentes de integranteEquipo asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de IntegranteEquipo asociados al Docente indicado
     */
    List<IntegranteEquipo> buscarPorDocente(Docente docente);

}
