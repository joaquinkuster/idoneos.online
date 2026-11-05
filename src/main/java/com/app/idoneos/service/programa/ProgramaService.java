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

/**
 * Servicio para gestionar las operaciones relacionadas con el programa.
 */
public interface ProgramaService extends CrudService<Programa> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de programa asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Programa asociados al Curso indicado
     */
    List<Programa> buscarPorCurso(Curso curso);

    /**
     * CU-15: Busca los programas activos de un curso por nombre y estado, del más reciente al más antiguo. Los
     * programas dados de baja no se incluyen.
     *
     * @param curso  el curso
     * @param nombre parte del nombre, sin distinguir mayúsculas ni tildes (opcional)
     * @param estado "Vigente" o "Anterior" (opcional)
     * @return los programas que cumplen los criterios
     */
    List<Programa> buscarConFiltros(Curso curso, String nombre, String estado);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-16: Registra un programa para un curso. Pasa a ser el vigente del curso y el programa por defecto del docente
     * que lo registra. Si se indica un programa de origen, se copia su cronograma.
     *
     * @param docente          el docente que registra
     * @param cursoId          el identificador del curso
     * @param programaOrigenId el programa del que se parte (opcional)
     * @param nombre           el nombre (opcional: si no se indica, es "Versión N")
     * @param descripcion      qué cambia respecto de la versión anterior (opcional)
     * @return el programa registrado
     * @throws IllegalArgumentException si supera el máximo, el curso no está activo o el docente no participa en el curso
     */
    Programa registrar(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion);

    /**
     * CU-16: Registra un programa con su cronograma de una vez (lo arma el asistente de programas: datos, cronograma y
     * contenido). Si se indican temas, estos y sus días reemplazan al cronograma del programa de origen.
     *
     * @param docente          el docente que registra
     * @param cursoId          el identificador del curso
     * @param programaOrigenId el programa del que se parte (opcional)
     * @param nombre           el nombre (opcional: si no se indica, es "Versión N")
     * @param descripcion      qué cambia respecto de la versión anterior (opcional)
     * @param temaIds          los temas del cronograma, en orden ({@code null} para conservar el del programa de origen)
     * @param dias             los días de cada tema, en el mismo orden
     * @return el programa registrado
     * @throws IllegalArgumentException si algún dato no es válido
     */
    Programa registrar(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion, List<Integer> temaIds, List<String> dias);

    /**
     * Un programa nuevo o modificado que llega de la ventana de programas.
     *
     * @param id          el identificador del programa ({@code null} si es nuevo)
     * @param origenId    el programa del que parte uno nuevo (opcional)
     * @param nombre      el nombre
     * @param descripcion la descripción (opcional)
     */
    record CambioDePrograma(Integer id, Integer origenId, String nombre, String descripcion) {
    }

    /**
     * Guarda de una vez los programas nuevos y los modificados de un curso: o se guardan todos o no se guarda ninguno.
     *
     * @param docente el docente que guarda (debe participar en el curso)
     * @param cursoId el identificador del curso
     * @param cambios los programas nuevos (sin identificador) y los modificados (con identificador)
     * @return la cantidad de programas registrados y modificados
     * @throws IllegalArgumentException si algún cambio no es válido; el mensaje nombra el programa
     */
    int guardarCambios(Docente docente, Integer cursoId, List<CambioDePrograma> cambios);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-17: Modifica los datos y el cronograma de un programa que no tiene inscripciones activas.
     *
     * @param id          el identificador del programa
     * @param docente     el docente que modifica
     * @param nombre      el nombre
     * @param descripcion qué cambia respecto de la versión anterior (opcional)
     * @param temaIds     los temas del cronograma, en orden ({@code null} para no tocar el cronograma)
     * @param dias        los días de cada tema, en el mismo orden
     * @return el programa modificado
     * @throws IllegalArgumentException si algún dato no es válido o el programa tiene inscripciones activas
     */
    Programa modificar(Integer id, Docente docente, String nombre, String descripcion, List<Integer> temaIds,
            List<String> dias);

    /**
     * CU-17: Modifica los datos de un programa que no tiene inscripciones activas.
     *
     * @param id           el identificador del programa
     * @param docente      el docente que modifica
     * @param nombre       el nombre
     * @param descripcion  qué cambia respecto de la versión anterior (opcional)
     * @return el programa modificado
     * @throws IllegalArgumentException si el programa no está activo, tiene inscripciones activas, falta el nombre o
     *                                  el docente no participa en el curso
     */
    Programa modificar(Integer id, Docente docente, String nombre, String descripcion);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-18: Da de baja un programa que no tiene cohortes activas (baja lógica).
     *
     * @param id      el identificador del programa
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si el programa no está activo, tiene cohortes activas o el docente no participa
     *                                  en el curso
     */
    void darDeBaja(Integer id, Docente docente);

    /**
     * CU-18: Da de baja varios programas a la vez, todos o ninguno: si alguno no puede darse de baja (no está activo, el
     * docente no participa en su curso o tiene cohortes activas), no se da de baja ninguno.
     *
     * @param ids     los identificadores de los programas
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no se seleccionó ningún programa o alguno no puede darse de baja
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);

    /**
     * Un programa que quedaría por debajo del mínimo.
     *
     * @param programa el programa afectado
     * @param minimo   el mínimo de temas con material publicado y autoevaluación que exige el sistema
     * @param quedaria la cantidad de temas que conservaría
     */
    public record Bloqueo(Programa programa, int minimo, int quedaria) {

        /** El mínimo expresado en temas, por ejemplo "2 temas". */
        public String minimoEnTemas() {
            return MensajeErrorUtil.formatearCantidad(minimo, "tema", "temas");
        }
    }

    /** El mínimo de temas con material publicado y autoevaluación que exige el sistema. */
    int buscarMinimoDeContenido();

    /**
     * Indica si una operación dejaría al programa por debajo del mínimo. Solo cuenta si el programa tiene cohortes en
     * curso y si la operación es la que le quita contenido: un programa que ya estaba por debajo y no empeora no se bloquea.
     *
     * @param programa           El programa que se verifica.
     * @param materialesQuitados Los materiales que dejarían de estar publicados.
     * @param temasQuitadas   Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo, o vacío si el programa se mantiene por encima del mínimo.
     */
    Optional<Bloqueo> verificarMinimoDeContenido(Programa programa, Collection<Integer> materialesQuitados,
            Collection<Integer> temasQuitadas);

    /**
     * Indica si quitar temas del cronograma dejaría al programa con cohortes en curso con menos temas que el mínimo.
     * Cuenta solo temas del cronograma, sin mirar su contenido. Si el programa ya estaba por debajo y la operación no
     * empeora, no se bloquea.
     *
     * @param programa         El programa que se verifica.
     * @param temasQuitadas Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo (con la cantidad de temas que quedarían), o vacío si el programa conserva el mínimo.
     */
    Optional<Bloqueo> verificarMinimoDeTemas(Programa programa, Collection<Integer> temasQuitadas);

    /**
     * Motivo (sin punto final) por el que no se puede quitar el material de un tema, con la misma oración que usan las
     * autoevaluaciones: es el único visible del tema y su programa tiene cohortes en curso.
     *
     * @param tema             El tema del material.
     * @param materialesQuitados Los materiales que dejarían de estar publicados en la operación.
     * @return El motivo, o vacío si el programa se mantiene por encima del mínimo.
     */
    Optional<String> buscarMotivoDeMinimoPorRecurso(Tema tema, Collection<Integer> materialesQuitados);
}
