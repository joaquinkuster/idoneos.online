package com.app.idoneos.servicio.Pregunta;

import java.util.List;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;

/**
 * Servicio para gestionar las operaciones relacionadas con la pregunta.
 */
public interface PreguntaServicio {

    /**
     * Busca los registros vigentes de pregunta asociados a pool.
     *
     * @param pool el registro de Pool asociado
     * @return una lista de Pregunta asociados al Pool indicado
     */
    List<Pregunta> buscarPorPool(Pool pool);
}
