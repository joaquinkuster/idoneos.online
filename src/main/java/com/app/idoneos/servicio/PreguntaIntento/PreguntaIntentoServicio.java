package com.app.idoneos.servicio.PreguntaIntento;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.PreguntaIntento;

/**
 * Servicio para gestionar las operaciones relacionadas con la pregunta del intento.
 */
public interface PreguntaIntentoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros de preguntaIntento asociados a intentoAutoevaluacion.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @return una lista de PreguntaIntento asociados al IntentoAutoevaluacion indicado
     */
    List<PreguntaIntento> buscarPorIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion);

    /**
     * Busca los registros de preguntaIntento asociados a pregunta.
     *
     * @param pregunta el registro de Pregunta asociado
     * @return una lista de PreguntaIntento asociados al Pregunta indicado
     */
    List<PreguntaIntento> buscarPorPregunta(Pregunta pregunta);

    /**
     * Busca la pregunta del intento a partir de la combinación única de sus relaciones.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @param pregunta el registro de Pregunta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<PreguntaIntento> buscarPorIntentoAutoevaluacionYPregunta(IntentoAutoevaluacion intentoAutoevaluacion, Pregunta pregunta);
}
