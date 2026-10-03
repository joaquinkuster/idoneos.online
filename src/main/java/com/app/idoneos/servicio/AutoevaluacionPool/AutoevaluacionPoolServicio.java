package com.app.idoneos.servicio.AutoevaluacionPool;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.AutoevaluacionPool;
import com.app.idoneos.modelo.Pool;

/**
 * Servicio para gestionar las operaciones relacionadas con la relación entre autoevaluación y pool.
 */
public interface AutoevaluacionPoolServicio {

    /**
     * Busca los registros de autoevaluacionPool asociados a pool.
     *
     * @param pool el registro de Pool asociado
     * @return una lista de AutoevaluacionPool asociados al Pool indicado
     */
    List<AutoevaluacionPool> buscarPorPool(Pool pool);

    /**
     * Busca los registros de autoevaluacionPool asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de AutoevaluacionPool asociados al Autoevaluacion indicado
     */
    List<AutoevaluacionPool> buscarPorAutoevaluacion(Autoevaluacion autoevaluacion);

    /**
     * Busca la relación entre autoevaluación y pool a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @param pool el registro de Pool asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<AutoevaluacionPool> buscarPorAutoevaluacionYPool(Autoevaluacion autoevaluacion, Pool pool);
}
