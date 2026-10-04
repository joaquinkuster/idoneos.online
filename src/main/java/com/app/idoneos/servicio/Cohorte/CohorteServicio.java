package com.app.idoneos.servicio.Cohorte;

import java.time.LocalDate;
import com.app.idoneos.modelo.Docente;
import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con la cohorte.
 */
public interface CohorteServicio {

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    List<Cohorte> buscarPorPrograma(Programa programa);

    /**
     * Busca cohortes aplicando filtros opcionales. Incluye las dadas de baja, que se ordenan siempre al final.
     *
     * @param idPrograma identificador del programa (opcional)
     * @param texto parte del nombre del curso o del programa (opcional)
     * @param estado estado de la cohorte: Abierta, En dictado, Finalizada, Próxima o Dada de baja (opcional)
     * @param desde fecha desde la que la inscripción debe estar abierta (opcional)
     * @param hasta fecha hasta la que la inscripción debe estar abierta (opcional)
     * @param orden el orden de los resultados: "recientes" (inicio de inscripción más nuevo primero, por defecto)
     *              o "curso" (nombre del curso A–Z); las dadas de baja van siempre al final
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que el docente participa
     * @return una lista de cohortes que cumplen los criterios
     */
    List<Cohorte> buscarConFiltros(Integer idPrograma, String texto, String estado, LocalDate desde, LocalDate hasta,
            String orden, Docente soloDeDocente);

    /**
     * Registra una cohorte para un programa activo.
     *
     * @param idPrograma el identificador del programa
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param semanasAcceso las semanas de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Cohorte registrarCohorte(Integer idPrograma, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer semanasAcceso, Integer cupoMaximo);

    /**
     * Modifica una cohorte activa sin inscripciones activas asociadas.
     *
     * @param idCohorte el identificador de la cohorte
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param semanasAcceso las semanas de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Cohorte modificarCohorte(Integer idCohorte, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer semanasAcceso, Integer cupoMaximo);

    /**
     * Da de baja una cohorte activa que no tenga inscripciones ni clases en vivo activas, y la desvincula
     * de los docentes que la tienen asignada por defecto.
     *
     * @param idCohorte el identificador de la cohorte
     * @throws IllegalArgumentException si la cohorte no está activa o tiene inscripciones o clases en vivo activas
     */
    void darDeBajaCohorte(Integer idCohorte);

    /**
     * Cambia el contexto de trabajo de un docente: establece la cohorte (y su programa) como la de trabajo
     * por defecto en el curso al que pertenece.
     *
     * @param cohorte la cohorte sobre la que el docente va a trabajar
     * @param docente el docente que cambia de contexto
     * @throws IllegalArgumentException si el docente no participa en el curso de la cohorte
     */
    void cambiarContextoDeTrabajo(Cohorte cohorte, Docente docente);

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja ninguno.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    void darDeBajaVarios(java.util.List<Integer> ids);
}
