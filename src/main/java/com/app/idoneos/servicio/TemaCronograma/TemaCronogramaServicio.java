package com.app.idoneos.servicio.TemaCronograma;

import java.util.List;
import com.app.idoneos.modelo.Docente;
import java.util.Optional;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;

/**
 * Servicio para gestionar las operaciones relacionadas con el tema del cronograma.
 */
public interface TemaCronogramaServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de temaCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de TemaCronograma asociados al Programa indicado
     */
    List<TemaCronograma> buscarPorPrograma(Programa programa);

    /**
     * Busca los registros vigentes de temaCronograma asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TemaCronograma asociados al Tema indicado
     */
    List<TemaCronograma> buscarPorTema(Tema tema);

    /**
     * Busca el tema del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TemaCronograma> buscarPorProgramaYTema(Programa programa, Tema tema);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-24: Modifica el cronograma de un programa: define qué temas lo componen, en qué orden y con cuántas semanas
     * cada una. Los temas que ya no figuran se dan de baja del cronograma (CU-22) y las nuevas se incorporan (CU-20).
     * Todo se hace en una sola operación: si algo falla, no se cambia nada.
     *
     * @param docente    el docente que modifica
     * @param programaId el identificador del programa
     * @param temaIds  los identificadores de los temas, en el orden del cronograma
     * @param semanas    las semanas de duración de cada tema, en el mismo orden
     * @throws IllegalArgumentException si el programa no está activo, tiene inscripciones activas, el docente no participa
     *                                  en el curso, alguna duración no es un entero mayor a cero o algún tema no es válida
     */
    void modificarCronograma(Docente docente, Integer programaId, List<Integer> temaIds, List<String> semanas);
}
