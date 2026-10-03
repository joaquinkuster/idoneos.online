package com.app.idoneos.servicio.UnidadCronograma;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;

/**
 * Servicio para gestionar las operaciones relacionadas con la unidad del cronograma.
 */
public interface UnidadCronogramaServicio {

    /**
     * Busca los registros vigentes de unidadCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de UnidadCronograma asociados al Programa indicado
     */
    List<UnidadCronograma> buscarPorPrograma(Programa programa);

    /**
     * Busca los registros vigentes de unidadCronograma asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de UnidadCronograma asociados al Unidad indicado
     */
    List<UnidadCronograma> buscarPorUnidad(Unidad unidad);

    /**
     * Busca la unidad del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<UnidadCronograma> buscarPorProgramaYUnidad(Programa programa, Unidad unidad);
}
