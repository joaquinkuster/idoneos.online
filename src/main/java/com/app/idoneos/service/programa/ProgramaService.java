package com.app.idoneos.service.programa;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.Tema;
import com.app.idoneos.util.MensajeErrorUtil;
import java.util.Collection;
import java.util.Optional;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import java.util.List;

/** Servicio del programa. */
public interface ProgramaService extends CrudService<Programa> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de programa asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Programa asociados al Curso indicado
     */
    List<Programa> buscarPorCurso(Curso curso);

    /**
     * Busca los programas activos de un curso por nombre y estado, del más reciente al más antiguo.
     *
     * @param curso el curso
     * @param nombre parte del nombre, sin distinguir mayúsculas ni tildes (opcional)
     * @param estado "Vigente" o "Anterior" (opcional)
     * @return los programas que cumplen los criterios
     */
    List<Programa> buscarConFiltros(Curso curso, String nombre, String estado);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Registra un programa para un curso.
     *
     * @param docente el docente que registra
     * @param cursoId el identificador del curso
     * @param programaOrigenId el programa del que se parte (opcional)
     * @param nombre el nombre (opcional: si no se indica, es "Versión N")
     * @param descripcion qué cambia respecto de la versión anterior (opcional)
     * @return el programa registrado
     * @throws IllegalArgumentException si supera el máximo, el curso no está activo o el docente no participa en el curso
     */
    Programa registrar(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion);

    /**
     * Registra un programa con su cronograma de una vez.
     *
     * @param docente el docente que registra
     * @param cursoId el identificador del curso
     * @param programaOrigenId el programa del que se parte (opcional)
     * @param nombre el nombre (opcional: si no se indica, es "Versión N")
     * @param descripcion qué cambia respecto de la versión anterior (opcional)
     * @param temaIds los temas del cronograma, en orden ({@code null} para conservar el del programa de origen)
     * @param dias los días de cada tema, en el mismo orden
     * @return el programa registrado
     * @throws IllegalArgumentException si algún dato no es válido
     */
    Programa registrar(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion, List<Integer> temaIds, List<String> dias);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica los datos y el cronograma de un programa que no tiene inscripciones activas.
     *
     * @param id el identificador del programa
     * @param docente el docente que modifica
     * @param nombre el nombre
     * @param descripcion qué cambia respecto de la versión anterior (opcional)
     * @param temaIds los temas del cronograma, en orden ({@code null} para no tocar el cronograma)
     * @param dias los días de cada tema, en el mismo orden
     * @return el programa modificado
     * @throws IllegalArgumentException si algún dato no es válido o el programa tiene inscripciones activas
     */
    Programa modificar(Integer id, Docente docente, String nombre, String descripcion, List<Integer> temaIds,
            List<String> dias);

    /**
     * Modifica los datos de un programa que no tiene inscripciones activas.
     *
     * @param id el identificador del programa
     * @param docente el docente que modifica
     * @param nombre el nombre
     * @param descripcion qué cambia respecto de la versión anterior (opcional)
     * @return el programa modificado
     * @throws IllegalArgumentException si el programa no está activo, tiene inscripciones activas, falta el nombre o el docente no participa en el curso
     */
    Programa modificar(Integer id, Docente docente, String nombre, String descripcion);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja un programa que no tiene cohortes activas (baja lógica).
     *
     * @param id el identificador del programa
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si el programa no está activo, tiene cohortes activas o el docente no participa en el curso
     */
    void darDeBaja(Integer id, Docente docente);

    /**
     * Da de baja varios programas a la vez, todos o ninguno.
     *
     * @param ids los identificadores de los programas
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no se seleccionó ningún programa o alguno no puede darse de baja
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);

    /**
     * Un programa que quedaría por debajo del mínimo.
     *
     * @param programa el programa afectado
     * @param minimo el mínimo de temas con material publicado y autoevaluación que exige el sistema
     * @param quedaria la cantidad de temas que conservaría
     */
    public record Bloqueo(Programa programa, int minimo, int quedaria) {

        /** Obtiene el mínimo expresado en temas, por ejemplo "2 temas". */
        public String minimoEnTemas() {
            return MensajeErrorUtil.formatearCantidad(minimo, "tema", "temas");
        }
    }

    /** El mínimo de temas con material publicado y autoevaluación que exige el sistema. */
    int buscarMinimoDeContenido();

    /**
     * Indica si una operación dejaría al programa por debajo del mínimo.
     *
     * @param programa El programa que se verifica.
     * @param materialesQuitados Los materiales que dejarían de estar publicados.
     * @param temasQuitadas Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo, o vacío si el programa se mantiene por encima del mínimo.
     */
    Optional<Bloqueo> verificarMinimoDeContenido(Programa programa, Collection<Integer> materialesQuitados,
            Collection<Integer> temasQuitadas);

    /**
     * Indica si quitar temas del cronograma dejaría al programa con cohortes en curso con menos temas que el mínimo.
     *
     * @param programa El programa que se verifica.
     * @param temasQuitadas Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo (con la cantidad de temas que quedarían), o vacío si el programa conserva el mínimo.
     */
    Optional<Bloqueo> verificarMinimoDeTemas(Programa programa, Collection<Integer> temasQuitadas);

    /**
     * Motivo (sin punto final) por el que no se puede quitar el material de un tema.
     *
     * @param tema El tema del material.
     * @param materialesQuitados Los materiales que dejarían de estar publicados en la operación.
     * @return El motivo, o vacío si el programa se mantiene por encima del mínimo.
     */
    Optional<String> buscarMotivoDeMinimoPorRecurso(Tema tema, Collection<Integer> materialesQuitados);

    /**
     * Motivo (sin punto final) por el que no se puede quitar el material de un tema porque el tema dejaría de tener el contenido que pide el diseño del curso (por ejemplo, su único material publicado o su única clase grabada) y su programa tiene cohortes en curso o inscripciones activas: el diseño pasaría de completo a incompleto.
     *
     * @param tema El tema del material.
     * @param materialesQuitados Los materiales que dejarían de estar publicados en la operación.
     * @return El motivo, o vacío si el tema conserva todo su contenido o ningún programa que lo incluye está en curso.
     */
    Optional<String> buscarMotivoDeDisenoIncompleto(Tema tema, Collection<Integer> materialesQuitados);
}
