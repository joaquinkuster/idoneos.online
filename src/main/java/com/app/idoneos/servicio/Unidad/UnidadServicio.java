package com.app.idoneos.servicio.Unidad;

import java.util.List;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con la unidad.
 */
public interface UnidadServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de unidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Unidad asociados al Curso indicado
     */
    List<Unidad> buscarPorCurso(Curso curso);

    /**
     * Indica si un usuario puede ver el contenido de una unidad (glosario, foro): el administrador, el docente que
     * participa en el curso y el alumno con la unidad habilitada.
     *
     * @param unidad  la unidad
     * @param usuario el usuario que quiere verlo
     * @return {@code true} si puede verlo
     */
    boolean puedeVerContenido(Unidad unidad, Usuario usuario);

    /**
     * CU-19: Busca las unidades del cronograma de un programa por título, en orden de dictado.
     *
     * @param programa el programa
     * @param titulo   parte del título, sin distinguir mayúsculas ni tildes (opcional)
     * @return las unidades del cronograma que cumplen el criterio
     */
    List<UnidadCronograma> buscarConFiltros(Programa programa, String titulo);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-20: Agrega una unidad al final del cronograma de un programa activo, con una semana de duración. Puede ser una
     * unidad nueva o una ya existente en otro programa del mismo curso.
     *
     * @param docente           el docente que agrega
     * @param programaId        el identificador del programa
     * @param unidadExistenteId la unidad de otro programa que se incorpora (opcional; si se indica se ignoran los datos
     *                          de la unidad nueva)
     * @param titulo            el título de la unidad nueva
     * @param descripcion       la descripción de la unidad nueva (opcional)
     * @param contenido         el contenido de la unidad nueva
     * @return la unidad agregada
     * @throws IllegalArgumentException si el programa no está activo, el docente no participa en el curso, faltan datos
     *                                  de la unidad nueva, el título ya existe o la unidad a incorporar no es válida
     */
    Unidad agregarUnidad(Docente docente, Integer programaId, Integer unidadExistenteId, String titulo,
            String descripcion, String contenido);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-21: Modifica el título, la descripción o el contenido de una unidad, mientras ningún programa que la incluya
     * tenga inscripciones activas.
     *
     * @param id          el identificador de la unidad
     * @param docente     el docente que modifica
     * @param titulo      el título
     * @param descripcion la descripción (opcional)
     * @param contenido   el contenido
     * @return la unidad modificada
     * @throws IllegalArgumentException si la unidad no está activa, tiene inscripciones activas asociadas, faltan datos,
     *                                  el título ya existe o el docente no participa en el curso
     */
    Unidad modificarUnidad(Integer id, Docente docente, String titulo, String descripcion, String contenido);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-22: Da de baja una unidad del cronograma de un programa. Si deja de formar parte del cronograma de cualquier
     * otro programa, la unidad queda además dada de baja. Las unidades restantes se renumeran sin dejar huecos.
     *
     * @param id         el identificador de la unidad
     * @param programaId el identificador del programa
     * @param docente    el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si el programa o la unidad no están activos, la unidad no pertenece al cronograma,
     *                                  el programa tiene inscripciones activas o el docente no participa en el curso
     */
    void darDeBajaUnidad(Integer id, Integer programaId, Docente docente);

    /**
     * CU-22: Da de baja varias unidades del cronograma de un programa, todas o ninguna.
     *
     * @param ids        los identificadores de las unidades
     * @param programaId el identificador del programa
     * @param docente    el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no se seleccionó ninguna unidad o alguna no puede darse de baja
     */
    void darDeBajaVarias(List<Integer> ids, Integer programaId, Docente docente);
}
