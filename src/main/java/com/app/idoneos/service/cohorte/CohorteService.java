package com.app.idoneos.service.cohorte;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import java.time.LocalDate;
import java.util.List;

/** Servicio de la cohorte. */
public interface CohorteService extends CrudService<Cohorte> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca las cohortes vigentes de los programas vigentes de un curso.
     *
     * @param curso el curso
     * @return una lista de Cohorte del curso indicado
     */
    List<Cohorte> buscarPorCurso(Curso curso);

    /**
     * Busca cohortes aplicando filtros opcionales.
     *
     * @param idPrograma identificador del programa (opcional)
     * @param texto parte del nombre del curso o del programa (opcional)
     * @param estado estado de la cohorte: Pendiente, Cancelada, Abierta, En dictado, Finalizada, Próxima o Dada de baja (opcional)
     * @param desde fecha desde la que la inscripción debe estar abierta (opcional)
     * @param hasta fecha hasta la que la inscripción debe estar abierta (opcional)
     * @param orden el orden de los resultados: "recientes" (inicio de inscripción más nuevo primero, por defecto) o "curso" (nombre del curso A,Z), las dadas de baja van siempre al final
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que el docente participa
     * @return una lista de cohortes que cumplen los criterios
     */
    List<Cohorte> buscarConFiltros(Integer idPrograma, String texto, String estado, LocalDate desde, LocalDate hasta,
            String orden, Docente soloDeDocente);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Registra una cohorte para un programa activo.
     *
     * @param idPrograma el identificador del programa
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo, el fin se calcula)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Cohorte registrar(Integer idPrograma, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, Integer accesoDias, Integer cupoMaximo);

    /**
     * Registra una cohorte de un curso activo, con o sin programa.
     *
     * @param idCurso el identificador del curso
     * @param idPrograma el identificador del programa (opcional, debe ser del curso)
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo, el fin se calcula)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Cohorte registrar(Integer idCurso, Integer idPrograma, LocalDate fechaInicioInscripcion,
            LocalDate fechaFinInscripcion, LocalDate fechaInicioDictado, Integer accesoDias,
            Integer cupoMaximo);

    /**
     * Devuelve qué le falta al contenido del programa para que una cohorte pueda estar Activa.
     *
     * @param programa el programa (puede ser {@code null})
     * @return los faltantes, vacío si el contenido está completo
     */
    List<String> faltantesDeContenido(Programa programa);

    /**
     * Días que como mínimo debe haber entre hoy y el inicio de la inscripción de una cohorte pendiente.
     *
     * @return el plazo mínimo en días
     */
    int plazoMinimoParaCompletarContenidoDias();

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Asigna (o cambia) el programa de una cohorte pendiente de contenido o con inscripción próxima.
     *
     * @param idCohorte el identificador de la cohorte
     * @param idPrograma el identificador del programa (debe ser del curso de la cohorte)
     * @param docente el docente que asigna el programa (debe participar en el curso)
     * @return la cohorte con el programa asignado
     * @throws IllegalArgumentException si la cohorte no está pendiente o el programa no es válido
     */
    Cohorte asignarPrograma(Integer idCohorte, Integer idPrograma, Docente docente);

    /**
     * Indica si un programa está completo.
     *
     * @param programa el programa (puede ser {@code null})
     * @return {@code true} si está completo
     */
    boolean estaCompleto(Programa programa);

    /**
     * Saca de "Pendiente de contenido" a toda cohorte cuyo programa ya tiene el contenido completo (el docente lo completó después de que la cohorte se registrara o se le asignara el programa): pasa al estado que le corresponde por sus fechas.
     *
     * @return la cantidad de cohortes que se activaron
     */
    int activarPendientesConContenidoCompleto();

    /**
     * Activa una cohorte pendiente si el diseño de su programa ya está completo (lo mismo que hace la tarea periódica, pero al momento: el docente no tiene que esperar al próximo ciclo para ver el cambio de estado).
     *
     * @param idCohorte el identificador de la cohorte
     * @return {@code true} si la cohorte salió de Pendiente de contenido
     */
    boolean activarSiEstaCompleta(Integer idCohorte);

    /**
     * Mueve de estado las cohortes que ya cumplieron una fecha: Inscripción próxima, abierta, cerrada.
     *
     * @return la cantidad de cohortes que cambiaron de estado
     */
    int actualizarEstadosPorFecha();

    /**
     * Cancela toda cohorte pendiente que llegó a la fecha de inicio de su inscripción sin tener el contenido de su programa completo: no se puede abrir una inscripción a un curso que todavía no está listo.
     *
     * @return la cantidad de cohortes que se cancelaron
     */
    int cancelarPendientesVencidas();

    /**
     * Modifica una cohorte activa sin inscripciones activas asociadas.
     *
     * @param idCohorte el identificador de la cohorte
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo, el fin se calcula)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Cohorte modificar(Integer idCohorte, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, Integer accesoDias, Integer cupoMaximo);

    /**
     * Cambia el contexto de trabajo de un docente.
     *
     * @param cohorte la cohorte sobre la que el docente va a trabajar
     * @param docente el docente que cambia de contexto
     * @throws IllegalArgumentException si el docente no participa en el curso de la cohorte
     */
    void cambiarContextoDeTrabajo(Cohorte cohorte, Docente docente);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja una cohorte activa que no tenga inscripciones ni clases en vivo activas.
     *
     * @param idCohorte el identificador de la cohorte
     * @throws IllegalArgumentException si la cohorte no está activa o tiene inscripciones o clases en vivo activas
     */
    void darDeBaja(Integer idCohorte);

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    void darDeBajaVarios(java.util.List<Integer> ids);
}
