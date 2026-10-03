package com.app.idoneos.servicio.IntentoAutoevaluacion;

import java.util.List;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;

/**
 * Servicio para gestionar las operaciones relacionadas con el intento de autoevaluación.
 */
public interface IntentoAutoevaluacionServicio {

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Inscripcion indicado
     */
    List<IntentoAutoevaluacion> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Autoevaluacion indicado
     */
    List<IntentoAutoevaluacion> buscarPorAutoevaluacion(Autoevaluacion autoevaluacion);
}
