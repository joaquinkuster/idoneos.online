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
     * @param contenido         el contenido del tema nueva (opcional: se puede completar después)
     * @return el tema agregado
     * @throws IllegalArgumentException si el programa no está activo, el docente no participa en el curso, faltan datos
     *                                  del tema nueva, el título ya existe o el tema a incorporar no es válida
     */
    Tema agregarTema(Docente docente, Integer programaId, Integer temaExistenteId, String titulo,
            String descripcion, String contenido);

    /**
     * CU-20: Igual que {@link #agregarTema(Docente, Integer, Integer, String, String, String)}, indicando también
     * cuántos días ocupa el tema en el cronograma del programa.
     *
     * @param duracionDias los días que dura el tema en el cronograma (opcional; si no se indica, los del parámetro
     *                     {@code cronograma.dias_por_tema_nuevo}, sin pasar de lo que queda hasta la duración del curso)
     * @return el tema agregado
     * @throws IllegalArgumentException además de los casos anteriores, si la duración no es válida, supera lo que queda
     *                                  hasta la duración del curso o deja el cronograma más largo que los días de
     *                                  acceso de una cohorte del programa
     */
    Tema agregarTema(Docente docente, Integer programaId, Integer temaExistenteId, String titulo,
            String descripcion, String contenido, Integer duracionDias);

    /**
     * Un cambio sobre un tema del curso: el alta de uno nuevo (sin {@code id}) o la modificación de uno existente.
     *
     * @param id          el identificador del tema ({@code null} si es nuevo)
     * @param titulo      el título
     * @param descripcion la descripción (opcional)
     * @param contenido   el contenido (opcional: se puede completar después)
     */
    record CambioDeTema(Integer id, String titulo, String descripcion, String contenido) {
    }

    /**
     * Registra un tema del curso, sin incorporarlo todavía al cronograma de ningún programa.
     *
     * @param docente     el docente que registra (debe participar en el curso)
     * @param cursoId     el identificador del curso
     * @param titulo      el título
     * @param descripcion la descripción (opcional)
     * @param contenido   el contenido (opcional)
     * @return el tema registrado
     * @throws IllegalArgumentException si el curso no está activo, el docente no participa, falta el título o ya existe
     */
    Tema registrarTema(Docente docente, Integer cursoId, String titulo, String descripcion, String contenido);

    /**
     * Guarda de una vez los temas nuevos y los modificados de un curso: o se guardan todos o no se guarda ninguno.
     *
     * @param docente     el docente que guarda (debe participar en el curso)
     * @param cursoId     el identificador del curso
     * @param cambios     los temas nuevos (sin identificador) y los modificados (con identificador)
     * @return la cantidad de temas registrados y modificados
     * @throws IllegalArgumentException si algún cambio no es válido; el mensaje nombra el tema
     */
    int guardarCambios(Docente docente, Integer cursoId, java.util.List<CambioDeTema> cambios);

    /**
     * Da de baja un tema del curso: lo quita del cronograma de todos los programas que lo incluyen y lo deja dado de baja.
     * No se puede si algún programa que lo incluye tiene inscripciones activas.
     *
     * @param id      el identificador del tema
     * @param docente el docente que da de baja
     * @throws IllegalArgumentException si el tema no está activo o es parte de un programa con inscripciones activas
     */
    void darDeBajaTemaDelCurso(Integer id, Docente docente);

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

    /**
     * CU-21: Igual que {@link #modificarTema(Integer, Docente, String, String, String)}, y cambia además cuántos
     * días ocupa el tema en el cronograma de un programa. La duración es de ese programa: el tema conserva la que tiene
     * en los demás.
     *
     * @param programaId   el programa cuyo cronograma contiene el tema
     * @param duracionDias los días que dura el tema en ese cronograma (opcional; si no se indica, se conserva la actual)
     * @return el tema modificado
     * @throws IllegalArgumentException además de los casos anteriores, si el tema no pertenece al cronograma del programa
     *                                  o la duración no es válida
     */
    Tema modificarTema(Integer id, Integer programaId, Docente docente, String titulo, String descripcion,
            String contenido, Integer duracionDias);

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
