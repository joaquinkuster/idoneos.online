package com.app.idoneos.service.tema;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import java.util.List;

/**
 * Servicio para gestionar las operaciones relacionadas con el tema.
 */
public interface TemaService extends CrudService<Tema> {

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
    Tema registrar(Docente docente, Integer cursoId, String titulo, String descripcion, String contenido);

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
    void darDeBajaPorCurso(Integer id, Docente docente);

    /**
     * Da de baja varios temas del curso a la vez, todos o ninguno (cada uno como {@link #darDeBajaPorCurso}).
     *
     * @param ids     los identificadores de los temas
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no hay temas seleccionados o alguno no se puede dar de baja
     */
    void darDeBajaVariasPorCurso(List<Integer> ids, Docente docente);

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
    Tema modificar(Integer id, Docente docente, String titulo, String descripcion, String contenido);

    // ---------------------------------------------------------------- Bajas

    /**
     * Quita varios temas del cronograma de un programa como parte de la modificación de todo el cronograma (la usa
     * {@code TemaCronogramaService.modificar}, que después reasigna los días y comprueba que la suma cierre con la del curso). A diferencia de
     * {@link #darDeBajaVarias}, no bloquea el quitar temas de un programa con cohortes activas por los días que dejan sin asignar.
     *
     * @param ids        los identificadores de los temas
     * @param programaId el identificador del programa
     * @param docente    el docente que modifica el cronograma
     * @throws IllegalArgumentException si alguna no puede quitarse (inscripciones o mínimo de temas)
     */
    void quitarVariasDelCronograma(List<Integer> ids, Integer programaId, Docente docente);
}
