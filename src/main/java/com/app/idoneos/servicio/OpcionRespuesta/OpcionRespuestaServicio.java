package com.app.idoneos.servicio.OpcionRespuesta;

import java.util.List;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Pregunta;

/**
 * Servicio para gestionar las operaciones relacionadas con la opción de respuesta.
 */
public interface OpcionRespuestaServicio {

    /**
     * Busca los registros vigentes de opcionRespuesta asociados a pregunta.
     *
     * @param pregunta el registro de Pregunta asociado
     * @return una lista de OpcionRespuesta asociados al Pregunta indicado
     */
    List<OpcionRespuesta> buscarPorPregunta(Pregunta pregunta);
}
