package com.app.idoneos.servicio.ParticipacionDocente;

import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con la participación docente.
 */
public interface ParticipacionDocenteServicio {

    /**
     * Busca los registros vigentes de participacionDocente asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de ParticipacionDocente asociados al Curso indicado
     */
    List<ParticipacionDocente> buscarPorCurso(Curso curso);

    /**
     * Busca los registros vigentes de participacionDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de ParticipacionDocente asociados al Docente indicado
     */
    List<ParticipacionDocente> buscarPorDocente(Docente docente);

    /**
     * Busca los registros vigentes de participacionDocente asociados a programa.
     *
     * @param programaPorDefecto el registro de Programa asociado
     * @return una lista de ParticipacionDocente asociados al Programa indicado
     */
    List<ParticipacionDocente> buscarPorProgramaPorDefecto(Programa programaPorDefecto);

    /**
     * Busca los registros vigentes de participacionDocente asociados a cohorte.
     *
     * @param cohortePorDefecto el registro de Cohorte asociado
     * @return una lista de ParticipacionDocente asociados al Cohorte indicado
     */
    List<ParticipacionDocente> buscarPorCohortePorDefecto(Cohorte cohortePorDefecto);
}
