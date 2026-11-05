package com.app.idoneos.service.temacronograma;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import java.util.List;

/**
 * Servicio para gestionar las operaciones relacionadas con el tema del cronograma.
 */
public interface TemaCronogramaService extends CrudService<TemaCronograma> {

    // ---------------------------------------------------------------- Lecturas


    /**
     * Busca los registros vigentes de temaCronograma asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TemaCronograma asociados al Tema indicado
     */
    List<TemaCronograma> buscarPorTema(Tema tema);


    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-24: Modifica el cronograma de un programa: define qué temas lo componen, en qué orden y con cuántos días
     * cada una. Los temas que ya no figuran se dan de baja del cronograma (CU-22) y las nuevas se incorporan (CU-20).
     * Todo se hace en una sola operación: si algo falla, no se cambia nada.
     *
     * @param docente    el docente que modifica
     * @param programaId el identificador del programa
     * @param temaIds  los identificadores de los temas, en el orden del cronograma
     * @param dias       los días de duración de cada tema, en el mismo orden
     * @throws IllegalArgumentException si el programa no está activo, tiene inscripciones activas, el docente no participa
     *                                  en el curso, alguna duración no es un entero mayor a cero o algún tema no es válida
     */
    void modificar(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias);
}
