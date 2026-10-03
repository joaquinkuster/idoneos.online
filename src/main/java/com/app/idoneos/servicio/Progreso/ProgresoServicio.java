package com.app.idoneos.servicio.Progreso;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con el progreso.
 */
public interface ProgresoServicio {

    /**
     * Busca los registros de progreso asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Progreso asociados al Unidad indicado
     */
    List<Progreso> buscarPorUnidad(Unidad unidad);

    /**
     * Busca los registros de progreso asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Progreso asociados al Inscripcion indicado
     */
    List<Progreso> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Progreso> buscarPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad);
}
