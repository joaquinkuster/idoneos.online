package com.app.idoneos.servicio.RespuestaForo;

import java.util.List;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.RespuestaForo;

/**
 * Servicio para gestionar las operaciones relacionadas con la respuesta del foro.
 */
public interface RespuestaForoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de respuestaForo asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de RespuestaForo asociados al DocenteCurso indicado
     */
    List<RespuestaForo> buscarPorDocenteCurso(DocenteCurso docenteCurso);

    /**
     * Busca los registros vigentes de respuestaForo asociados a consultaForo.
     *
     * @param consulta el registro de ConsultaForo asociado
     * @return una lista de RespuestaForo asociados al ConsultaForo indicado
     */
    List<RespuestaForo> buscarPorConsulta(ConsultaForo consulta);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-40: Registra la respuesta de un docente a una consulta. Solo lo hace un docente que participa en el curso.
     *
     * @param docente    el docente que responde
     * @param consultaId el identificador de la consulta
     * @param texto      el texto de la respuesta
     * @return la respuesta registrada
     * @throws IllegalArgumentException si falta el texto, supera el máximo, la consulta no está activa o el docente
     *                                  no participa en el curso
     */
    RespuestaForo registrarRespuesta(Docente docente, Integer consultaId, String texto);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-41: Modifica el texto de una respuesta propia, dentro del plazo de edición.
     *
     * @param id      el identificador de la respuesta
     * @param docente el docente que modifica
     * @param texto   el texto nuevo
     * @return la respuesta modificada
     * @throws IllegalArgumentException si no es suya, no está activa, pasó el plazo o el texto no es válido
     */
    RespuestaForo modificarRespuesta(Integer id, Docente docente, String texto);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-42: Da de baja una respuesta (baja lógica). Lo hace el administrador.
     *
     * @param id el identificador de la respuesta
     * @throws IllegalArgumentException si la respuesta no está activa
     */
    void darDeBajaRespuesta(Integer id);
}
