package com.app.idoneos.servicio.RespuestaForo;

import java.util.List;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.RespuestaForo;

/**
 * Servicio para gestionar las operaciones relacionadas con la respuesta del foro.
 */
public interface RespuestaForoServicio {

    /**
     * Busca los registros vigentes de respuestaForo asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de RespuestaForo asociados al ParticipacionDocente indicado
     */
    List<RespuestaForo> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente);

    /**
     * Busca los registros vigentes de respuestaForo asociados a consultaForo.
     *
     * @param consulta el registro de ConsultaForo asociado
     * @return una lista de RespuestaForo asociados al ConsultaForo indicado
     */
    List<RespuestaForo> buscarPorConsulta(ConsultaForo consulta);
}
