package com.app.idoneos.servicio.Unidad;

import java.util.List;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con la unidad.
 */
public interface UnidadServicio {

    /**
     * Busca los registros vigentes de unidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Unidad asociados al Curso indicado
     */
    List<Unidad> buscarPorCurso(Curso curso);
}
