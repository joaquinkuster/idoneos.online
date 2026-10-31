package com.app.idoneos.servicio.Programa;

import java.util.List;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con el programa.
 */
public interface ProgramaServicio {

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
    Programa registrarPrograma(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
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
    Programa registrarPrograma(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion, List<Integer> temaIds, List<String> dias);

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
    Programa modificarPrograma(Integer id, Docente docente, String nombre, String descripcion, List<Integer> temaIds,
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
    Programa modificarPrograma(Integer id, Docente docente, String nombre, String descripcion);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-18: Da de baja un programa que no tiene cohortes activas (baja lógica).
     *
     * @param id      el identificador del programa
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si el programa no está activo, tiene cohortes activas o el docente no participa
     *                                  en el curso
     */
    void darDeBajaPrograma(Integer id, Docente docente);

    /**
     * CU-18: Da de baja varios programas a la vez, todos o ninguno: si alguno no puede darse de baja (no está activo, el
     * docente no participa en su curso o tiene cohortes activas), no se da de baja ninguno.
     *
     * @param ids     los identificadores de los programas
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no se seleccionó ningún programa o alguno no puede darse de baja
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);
}
