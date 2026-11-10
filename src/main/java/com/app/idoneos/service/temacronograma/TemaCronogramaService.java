package com.app.idoneos.service.temacronograma;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import java.util.List;

/** Servicio del tema del cronograma. */
public interface TemaCronogramaService extends CrudService<TemaCronograma> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de temaCronograma asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TemaCronograma asociados al Tema indicado
     */
    List<TemaCronograma> buscarPorTema(Tema tema);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica el cronograma de un programa: define qué temas lo componen.
     *
     * @param docente el docente que modifica
     * @param programaId el identificador del programa
     * @param temaIds los identificadores de los temas, en el orden del cronograma
     * @param dias los días de duración de cada tema, en el mismo orden
     * @throws IllegalArgumentException si el programa no está activo, tiene inscripciones activas, el docente no participa en el curso, alguna duración no es un entero mayor a cero o algún tema no es válida
     */
    void modificar(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias);

    /**
     * Arma el cronograma de un programa que se está registrando: es un programa nuevo, sin cohortes.
     *
     * @param docente el docente que lo arma
     * @param programaId el programa nuevo
     * @param temaIds los temas, en orden
     * @param dias los días de cada tema
     */
    void definirDeProgramaNuevo(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias);
}
