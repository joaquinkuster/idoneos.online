package com.app.idoneos.servicio.Autoevaluacion;

import java.util.List;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con la autoevaluación.
 */
public interface AutoevaluacionServicio {

    /**
     * Busca los registros vigentes de autoevaluacion asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Autoevaluacion asociados al Unidad indicado
     */
    List<Autoevaluacion> buscarPorUnidad(Unidad unidad);
}
