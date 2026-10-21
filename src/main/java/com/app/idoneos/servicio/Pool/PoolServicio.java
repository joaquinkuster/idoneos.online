package com.app.idoneos.servicio.Pool;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con el pool de preguntas.
 */
public interface PoolServicio {

    /**
     * Pregunta que se carga en un pool (alta o modificación).
     *
     * @param id             identificador de la pregunta que ya existe en el pool, o {@code null} si es nueva
     * @param texto          enunciado
     * @param opcionMultiple {@code true} si admite varias opciones correctas; {@code false} si admite una sola
     *                       (opción única y verdadero/falso)
     * @param opciones       opciones de respuesta, en el orden en que se muestran
     */
    record SolicitudPregunta(Integer id, String texto, boolean opcionMultiple, List<SolicitudOpcion> opciones) {
    }

    /**
     * Opción de respuesta que se carga en una pregunta.
     *
     * @param id       identificador de la opción que ya existe en la pregunta, o {@code null} si es nueva
     * @param texto    texto de la opción
     * @param correcta si la opción es correcta
     */
    record SolicitudOpcion(Integer id, String texto, boolean correcta) {
    }

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el pool vigente por su identificador.
     *
     * @param id el identificador del pool
     * @return un {@link Optional} con el pool si existe y no está dado de baja
     */
    Optional<Pool> buscarPorId(Integer id);

    /**
     * Busca los registros vigentes de pool asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Pool asociados al Unidad indicado
     */
    List<Pool> buscarPorUnidad(Unidad unidad);

    /**
     * CU-60: Busca los pools de un curso (incluidos los dados de baja) aplicando filtros opcionales. Los dados de baja van
     * siempre al final.
     *
     * @param idCurso       identificador del curso (opcional)
     * @param idUnidad      identificador de la unidad (opcional)
     * @param nombre        parte del nombre (opcional)
     * @param baja          {@code true} solo dados de baja, {@code false} solo activos, {@code null} todos (opcional)
     * @param soloDeDocente si se indica, solo los pools de los cursos en los que participa ese docente
     * @return los pools que cumplen los criterios
     */
    List<Pool> buscarConFiltros(Integer idCurso, Integer idUnidad, String nombre, Boolean baja, Docente soloDeDocente);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-61: Crea un pool en una unidad activa, con sus preguntas y opciones de respuesta.
     *
     * @param docente   el docente que lo crea; debe participar en el curso como titular o ayudante
     * @param idUnidad  identificador de la unidad
     * @param nombre    nombre del pool
     * @param preguntas preguntas del pool (al menos una)
     * @return el pool registrado
     * @throws IllegalArgumentException si la unidad no está activa o los datos no son válidos
     */
    Pool registrarPool(Docente docente, Integer idUnidad, String nombre, List<SolicitudPregunta> preguntas);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-62: Modifica el nombre de un pool y agrega, edita o elimina sus preguntas y opciones. Las preguntas y opciones que
     * no figuran en la solicitud se dan de baja.
     *
     * @param idPool    identificador del pool
     * @param docente   el docente que lo modifica; debe participar en el curso como titular o ayudante
     * @param nombre    nombre del pool
     * @param preguntas preguntas que debe conservar el pool (al menos una)
     * @return el pool actualizado
     * @throws IllegalArgumentException si el pool no está activo, ya tiene intentos o los datos no son válidos
     */
    Pool modificarPool(Integer idPool, Docente docente, String nombre, List<SolicitudPregunta> preguntas);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-63: Da de baja un pool que no está asociado a ninguna autoevaluación activa.
     *
     * @param idPool  identificador del pool
     * @param docente el docente que lo da de baja, o {@code null} si es el administrador
     * @throws IllegalArgumentException si el pool no está activo o tiene autoevaluaciones asociadas
     */
    void darDeBajaPool(Integer idPool, Docente docente);

    /**
     * Da de baja varios pools. Es todo o nada: primero se valida cada uno y recién después se dan de baja.
     *
     * @param ids     identificadores de los pools
     * @param docente el docente que los da de baja, o {@code null} si es el administrador
     * @throws IllegalArgumentException si no se seleccionó ninguno o alguno no se puede dar de baja
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);
}
