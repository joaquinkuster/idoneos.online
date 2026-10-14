package com.app.idoneos.servicio.UnidadCronograma;

import java.util.List;
import com.app.idoneos.modelo.Docente;
import java.util.Optional;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;

/**
 * Servicio para gestionar las operaciones relacionadas con la unidad del cronograma.
 */
public interface UnidadCronogramaServicio {

    // ---------------------------------------------------------------- Lecturas

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

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-24: Modifica el cronograma de un programa: define qué unidades lo componen, en qué orden y con cuántas semanas
     * cada una. Las unidades que ya no figuran se dan de baja del cronograma (CU-22) y las nuevas se incorporan (CU-20).
     * Todo se hace en una sola operación: si algo falla, no se cambia nada.
     *
     * @param docente    el docente que modifica
     * @param programaId el identificador del programa
     * @param unidadIds  los identificadores de las unidades, en el orden del cronograma
     * @param semanas    las semanas de duración de cada unidad, en el mismo orden
     * @throws IllegalArgumentException si el programa no está activo, tiene inscripciones activas, el docente no participa
     *                                  en el curso, alguna duración no es un entero mayor a cero o alguna unidad no es válida
     */
    void modificarCronograma(Docente docente, Integer programaId, List<Integer> unidadIds, List<String> semanas);
}
