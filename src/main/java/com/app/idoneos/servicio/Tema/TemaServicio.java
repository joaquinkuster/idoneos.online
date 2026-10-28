package com.app.idoneos.servicio.Tema;

import java.util.List;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el tema.
 */
public interface TemaServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de tema asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Tema asociados al Curso indicado
     */
    List<Tema> buscarPorCurso(Curso curso);

    /**
     * Indica si un usuario puede ver el contenido de un tema (glosario, foro): el administrador, el docente que
     * participa en el curso y el alumno con el tema habilitado.
     *
     * @param tema  el tema
     * @param usuario el usuario que quiere verlo
     * @return {@code true} si puede verlo
     */
    boolean puedeVerContenido(Tema tema, Usuario usuario);

    /**
     * CU-19: Busca los temas del cronograma de un programa por título, en orden de dictado.
     *
     * @param programa el programa
     * @param titulo   parte del título, sin distinguir mayúsculas ni tildes (opcional)
     * @return los temas del cronograma que cumplen el criterio
     */
    List<TemaCronograma> buscarConFiltros(Programa programa, String titulo);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-20: Agrega un tema al final del cronograma de un programa activo, con siete días de duración. Puede ser una
     * tema nueva o una ya existente en otro programa del mismo curso.
     *
     * @param docente           el docente que agrega
     * @param programaId        el identificador del programa
     * @param temaExistenteId el tema de otro programa que se incorpora (opcional; si se indica se ignoran los datos
     *                          del tema nueva)
     * @param titulo            el título del tema nueva
     * @param descripcion       la descripción del tema nueva (opcional)
     * @param contenido         el contenido del tema nueva
     * @return el tema agregado
     * @throws IllegalArgumentException si el programa no está activo, el docente no participa en el curso, faltan datos
     *                                  del tema nueva, el título ya existe o el tema a incorporar no es válida
     */
    Tema agregarTema(Docente docente, Integer programaId, Integer temaExistenteId, String titulo,
            String descripcion, String contenido);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-21: Modifica el título, la descripción o el contenido de un tema, mientras ningún programa que la incluya
     * tenga inscripciones activas.
     *
     * @param id          el identificador del tema
     * @param docente     el docente que modifica
     * @param titulo      el título
     * @param descripcion la descripción (opcional)
     * @param contenido   el contenido
     * @return el tema modificado
     * @throws IllegalArgumentException si el tema no está activa, tiene inscripciones activas asociadas, faltan datos,
     *                                  el título ya existe o el docente no participa en el curso
     */
    Tema modificarTema(Integer id, Docente docente, String titulo, String descripcion, String contenido);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-22: Da de baja un tema del cronograma de un programa. Si deja de formar parte del cronograma de cualquier
     * otro programa, el tema queda además dada de baja. Los temas restantes se renumeran sin dejar huecos.
     *
     * @param id         el identificador del tema
     * @param programaId el identificador del programa
     * @param docente    el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si el programa o el tema no están activos, el tema no pertenece al cronograma,
     *                                  el programa tiene inscripciones activas o el docente no participa en el curso
     */
    void darDeBajaTema(Integer id, Integer programaId, Docente docente);

    /**
     * CU-22: Da de baja varios temas del cronograma de un programa, todas o ninguna.
     *
     * @param ids        los identificadores de los temas
     * @param programaId el identificador del programa
     * @param docente    el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no se seleccionó ningún tema o alguna no puede darse de baja
     */
    void darDeBajaVarias(List<Integer> ids, Integer programaId, Docente docente);
}
