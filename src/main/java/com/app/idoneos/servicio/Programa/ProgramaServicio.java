package com.app.idoneos.servicio.Programa;

import java.util.List;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con el programa.
 */
public interface ProgramaServicio {

    /**
     * Busca los registros vigentes de programa asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Programa asociados al Curso indicado
     */
    List<Programa> buscarPorCurso(Curso curso);
}
