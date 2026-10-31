package com.app.idoneos.servicio.DocenteCurso;

import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con la participación docente.
 */
public interface DocenteCursoServicio {

    // ---------------------------------------------------------------- Lecturas

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

    /**
     * Busca los registros vigentes de docenteCurso asociados a cohorte.
     *
     * @param cohortePorDefecto el registro de Cohorte asociado
     * @return una lista de DocenteCurso asociados al Cohorte indicado
     */
    List<DocenteCurso> buscarPorCohortePorDefecto(Cohorte cohortePorDefecto);
}
