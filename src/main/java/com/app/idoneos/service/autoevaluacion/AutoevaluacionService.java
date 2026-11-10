package com.app.idoneos.service.autoevaluacion;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Tema;
import java.util.List;
import java.util.Optional;

/** Servicio de la autoevaluación. */
public interface AutoevaluacionService extends CrudService<Autoevaluacion> {

    /**
     * Datos que el docente carga en una autoevaluación (alta o modificación).
     *
     * @param nombre nombre de la autoevaluación
     * @param tiempoLimite tiempo límite en minutos (entero mayor a cero)
     * @param cantidadPreguntas cantidad de preguntas que se sortean (entero mayor a cero)
     * @param notaMinima nota mínima para aprobar (entero mayor a cero y hasta 10)
     * @param intentosPermitidos cantidad de intentos permitidos, vacío si no hay límite
     * @param fechaApertura fecha y hora de apertura ({@code yyyy-MM-ddTHH:mm})
     * @param fechaCierre fecha y hora de cierre, vacío si no cierra
     * @param descuentoPorError si cada opción incorrecta resta
     * @param navegacionLibre si el alumno puede desplazarse libremente entre las preguntas
     * @param permiteSalir si el alumno puede salir del intento en curso
     * @param oculto si la autoevaluación queda oculta para los alumnos
     * @param idsPools pools de los que se sortea (al menos uno)
     */
    record SolicitudAutoevaluacion(String nombre, String tiempoLimite, String cantidadPreguntas, String notaMinima,
            String intentosPermitidos, String fechaApertura, String fechaCierre, boolean descuentoPorError,
            boolean navegacionLibre, boolean permiteSalir, boolean oculto, List<Integer> idsPools) {
    }

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca la autoevaluación vigente por su identificador.
     *
     * @param id el identificador de la autoevaluación
     * @return un {@link Optional} con la autoevaluación si existe y no está dada de baja
     */
    Optional<Autoevaluacion> buscarPorId(Integer id);

    /**
     * Busca los registros vigentes de autoevaluacion asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Autoevaluacion asociados al Tema indicado
     */
    List<Autoevaluacion> buscarPorTema(Tema tema);

    /**
     * Busca las autoevaluaciones de un curso (incluidas las dadas de baja) aplicando filtros opcionales.
     *
     * @param idCurso identificador del curso (opcional)
     * @param idTema identificador del tema (opcional)
     * @param nombre parte del nombre (opcional)
     * @param baja {@code true} solo dadas de baja, {@code false} solo vigentes, {@code null} todas (opcional)
     * @param soloDeDocente si se indica, solo las de los cursos en los que participa ese docente
     * @return las autoevaluaciones que cumplen los criterios
     */
    List<Autoevaluacion> buscarConFiltros(Integer idCurso, Integer idTema, String nombre, Boolean baja,
            Docente soloDeDocente);

    /**
     * Obtiene los temas cuyos autoevaluaciones pueden usar un pool.
     *
     * @param pool el pool
     * @return los temas que pueden usarlo, la propia primero
     */
    List<Tema> temasQuePuedenUsar(Pool pool);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Crea una autoevaluación en un tema activo.
     *
     * @param docente el docente que la crea, debe participar en el curso como titular o ayudante
     * @param idTema identificador del tema
     * @param datos los datos de la autoevaluación
     * @return la autoevaluación registrada
     * @throws IllegalArgumentException si el tema no está activa o los datos no son válidos
     */
    Autoevaluacion registrar(Docente docente, Integer idTema, SolicitudAutoevaluacion datos);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica una autoevaluación.
     *
     * @param idAutoevaluacion identificador de la autoevaluación
     * @param docente el docente que la modifica, debe participar en el curso como titular o ayudante
     * @param datos los datos de la autoevaluación
     * @return la autoevaluación actualizada
     * @throws IllegalArgumentException si no está activa o los datos no son válidos
     */
    Autoevaluacion modificar(Integer idAutoevaluacion, Docente docente, SolicitudAutoevaluacion datos);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja una autoevaluación sin intentos que no sea la última visible de un tema con cohortes en curso.
     *
     * @param idAutoevaluacion identificador de la autoevaluación
     * @param docente el docente que la da de baja, o {@code null} si es el administrador
     * @throws IllegalArgumentException si no está activa o no se puede dar de baja
     */
    void darDeBaja(Integer idAutoevaluacion, Docente docente);

    /**
     * Da de baja varias autoevaluaciones.
     *
     * @param ids identificadores de las autoevaluaciones
     * @param docente el docente que las da de baja, o {@code null} si es el administrador
     * @throws IllegalArgumentException si no se seleccionó ninguna o alguna no se puede dar de baja
     */
    void darDeBajaVarias(List<Integer> ids, Docente docente);
}
