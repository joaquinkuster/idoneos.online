package com.app.idoneos.servicio.ConsultaForo;

import java.util.List;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con la consulta del foro.
 */
public interface ConsultaForoServicio {

    /**
     * Busca los registros vigentes de consultaForo asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de ConsultaForo asociados al Unidad indicado
     */
    List<ConsultaForo> buscarPorUnidad(Unidad unidad);

    /**
     * Busca los registros vigentes de consultaForo asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de ConsultaForo asociados al Inscripcion indicado
     */
    List<ConsultaForo> buscarPorInscripcion(Inscripcion inscripcion);
}
