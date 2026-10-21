package com.app.idoneos.servicio.RespuestaIntento;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.modelo.RespuestaIntento;

/**
 * Servicio para gestionar las operaciones relacionadas con la respuesta del intento.
 */
public interface RespuestaIntentoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros de respuestaIntento asociados a preguntaIntento.
     *
     * @param preguntaIntento el registro de PreguntaIntento asociado
     * @return una lista de RespuestaIntento asociados al PreguntaIntento indicado
     */
    List<RespuestaIntento> buscarPorPreguntaIntento(PreguntaIntento preguntaIntento);

    /**
     * Busca los registros de respuestaIntento asociados a opcionRespuesta.
     *
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return una lista de RespuestaIntento asociados al OpcionRespuesta indicado
     */
    List<RespuestaIntento> buscarPorOpcionRespuesta(OpcionRespuesta opcionRespuesta);

    /**
     * Busca la respuesta del intento a partir de la combinación única de sus relaciones.
     *
     * @param preguntaIntento el registro de PreguntaIntento asociado
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<RespuestaIntento> buscarPorPreguntaIntentoYOpcionRespuesta(PreguntaIntento preguntaIntento, OpcionRespuesta opcionRespuesta);
}
