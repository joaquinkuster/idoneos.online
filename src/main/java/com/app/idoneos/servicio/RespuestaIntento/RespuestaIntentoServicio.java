package com.app.idoneos.servicio.RespuestaIntento;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.RespuestaIntento;

/**
 * Servicio para gestionar las operaciones relacionadas con la respuesta del intento.
 */
public interface RespuestaIntentoServicio {

    /**
     * Busca los registros de respuestaIntento asociados a intentoAutoevaluacion.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @return una lista de RespuestaIntento asociados al IntentoAutoevaluacion indicado
     */
    List<RespuestaIntento> buscarPorIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion);

    /**
     * Busca los registros de respuestaIntento asociados a opcionRespuesta.
     *
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return una lista de RespuestaIntento asociados al OpcionRespuesta indicado
     */
    List<RespuestaIntento> buscarPorOpcionRespuesta(OpcionRespuesta opcionRespuesta);

    /**
     * Busca la respuesta del intento a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<RespuestaIntento> buscarPorIntentoAutoevaluacionYOpcionRespuesta(IntentoAutoevaluacion intentoAutoevaluacion, OpcionRespuesta opcionRespuesta);
}
