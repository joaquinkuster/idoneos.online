package com.app.idoneos.servicio.Autoevaluacion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con la autoevaluación.
 */
public interface AutoevaluacionServicio {

    /**
     * Datos que el docente carga en una autoevaluación (alta o modificación). Llegan como texto para poder informar cada
     * error de formato con su campo.
     *
     * @param nombre             nombre de la autoevaluación
     * @param tiempoLimite       tiempo límite en minutos (entero mayor a cero)
     * @param cantidadPreguntas  cantidad de preguntas que se sortean (entero mayor a cero)
     * @param notaMinima         nota mínima para aprobar (entero mayor a cero y hasta 10)
     * @param intentosPermitidos cantidad de intentos permitidos; vacío si no hay límite
     * @param fechaApertura      fecha y hora de apertura ({@code yyyy-MM-ddTHH:mm})
     * @param fechaCierre        fecha y hora de cierre; vacío si no cierra
     * @param descuentoPorError  si cada opción incorrecta resta
     * @param navegacionLibre    si el alumno puede desplazarse libremente entre las preguntas
     * @param permiteSalir       si el alumno puede salir del intento en curso
     * @param oculto             si la autoevaluación queda oculta para los alumnos
     * @param idsPools           pools de los que se sortea (al menos uno)
     */
    record SolicitudAutoevaluacion(String nombre, String tiempoLimite, String cantidadPreguntas, String notaMinima,
            String intentosPermitidos, String fechaApertura, String fechaCierre, boolean descuentoPorError,
            boolean navegacionLibre, boolean permiteSalir, boolean oculto, List<Integer> idsPools) {
    }

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la autoevaluación vigente por su identificador.
     *
     * @param id el identificador de la autoevaluación
     * @return un {@link Optional} con la autoevaluación si existe y no está dada de baja
     */
    Optional<Autoevaluacion> buscarPorId(Integer id);

    /**
     * Busca los registros vigentes de autoevaluacion asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Autoevaluacion asociados al Unidad indicado
     */
    List<Autoevaluacion> buscarPorUnidad(Unidad unidad);

    /**
     * CU-64: Busca las autoevaluaciones de un curso (incluidas las dadas de baja) aplicando filtros opcionales. Las dadas
     * de baja van siempre al final.
     *
     * @param idCurso       identificador del curso (opcional)
     * @param idUnidad      identificador de la unidad (opcional)
     * @param nombre        parte del nombre (opcional)
     * @param baja          {@code true} solo dadas de baja, {@code false} solo vigentes, {@code null} todas (opcional)
     * @param soloDeDocente si se indica, solo las de los cursos en los que participa ese docente
     * @return las autoevaluaciones que cumplen los criterios
     */
    List<Autoevaluacion> buscarConFiltros(Integer idCurso, Integer idUnidad, String nombre, Boolean baja,
            Docente soloDeDocente);

    /**
     * Obtiene las unidades cuyas autoevaluaciones pueden usar un pool: la propia y las que vienen después en el
     * cronograma de los programas que incluyen la unidad del pool. Una autoevaluación puede combinar pools de su unidad y de
     * las unidades anteriores (para un examen de la unidad, un repaso o una evaluación final), nunca de las posteriores.
     *
     * @param pool el pool
     * @return las unidades que pueden usarlo, la propia primero
     */
    List<Unidad> unidadesQuePuedenUsar(Pool pool);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-65: Crea una autoevaluación en una unidad activa, asociada a uno o más pools de su unidad o de unidades anteriores.
     *
     * @param docente  el docente que la crea; debe participar en el curso como titular o ayudante
     * @param idUnidad identificador de la unidad
     * @param datos    los datos de la autoevaluación
     * @return la autoevaluación registrada
     * @throws IllegalArgumentException si la unidad no está activa o los datos no son válidos
     */
    Autoevaluacion registrarAutoevaluacion(Docente docente, Integer idUnidad, SolicitudAutoevaluacion datos);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-66: Modifica una autoevaluación. Mientras no registre intentos pueden modificarse todos sus datos; una vez
     * registrado al menos uno solo pueden extenderse la fecha de cierre, ocultarse o mostrarse y ampliarse la cantidad de
     * intentos permitidos. Al ocultarla se verifica el progreso de los alumnos de la unidad.
     *
     * @param idAutoevaluacion identificador de la autoevaluación
     * @param docente          el docente que la modifica; debe participar en el curso como titular o ayudante
     * @param datos            los datos de la autoevaluación
     * @return la autoevaluación actualizada
     * @throws IllegalArgumentException si no está activa o los datos no son válidos
     */
    Autoevaluacion modificarAutoevaluacion(Integer idAutoevaluacion, Docente docente, SolicitudAutoevaluacion datos);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-67: Da de baja una autoevaluación sin intentos que no sea la última visible de una unidad con cohortes en curso.
     *
     * @param idAutoevaluacion identificador de la autoevaluación
     * @param docente          el docente que la da de baja, o {@code null} si es el administrador
     * @throws IllegalArgumentException si no está activa o no se puede dar de baja
     */
    void darDeBajaAutoevaluacion(Integer idAutoevaluacion, Docente docente);

    /**
     * Da de baja varias autoevaluaciones. Es todo o nada: primero se valida el conjunto y recién después se dan de baja.
     *
     * @param ids     identificadores de las autoevaluaciones
     * @param docente el docente que las da de baja, o {@code null} si es el administrador
     * @throws IllegalArgumentException si no se seleccionó ninguna o alguna no se puede dar de baja
     */
    void darDeBajaVarias(List<Integer> ids, Docente docente);
}
