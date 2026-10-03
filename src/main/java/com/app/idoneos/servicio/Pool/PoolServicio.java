package com.app.idoneos.servicio.Pool;

import java.util.List;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con el pool.
 */
public interface PoolServicio {

    /**
     * Busca los registros vigentes de pool asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Pool asociados al Unidad indicado
     */
    List<Pool> buscarPorUnidad(Unidad unidad);
}
