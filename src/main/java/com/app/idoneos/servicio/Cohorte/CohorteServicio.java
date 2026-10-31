package com.app.idoneos.servicio.Cohorte;

import com.app.idoneos.modelo.Curso;
import java.time.LocalDate;
import com.app.idoneos.modelo.Docente;
import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con la cohorte.
 */
public interface CohorteServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    List<Cohorte> buscarPorPrograma(Programa programa);

    /**
     * Busca las cohortes vigentes de los programas vigentes de un curso.
     *
     * @param curso el curso
     * @return una lista de Cohorte del curso indicado
     */
    List<Cohorte> buscarPorCurso(Curso curso);

    /**
     * Busca cohortes aplicando filtros opcionales. Incluye las dadas de baja, que se ordenan siempre al final.
     *
     * @param idPrograma identificador del programa (opcional)
     * @param texto parte del nombre del curso o del programa (opcional)
     * @param estado estado de la cohorte: Pendiente, Cancelada, Abierta, En dictado, Finalizada, Próxima o Dada de baja (opcional)
     * @param desde fecha desde la que la inscripción debe estar abierta (opcional)
     * @param hasta fecha hasta la que la inscripción debe estar abierta (opcional)
     * @param orden el orden de los resultados: "recientes" (inicio de inscripción más nuevo primero, por defecto)
     *              o "curso" (nombre del curso A–Z); las dadas de baja van siempre al final
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que el docente participa
     * @return una lista de cohortes que cumplen los criterios
     */
    List<Cohorte> buscarConFiltros(Integer idPrograma, String texto, String estado, LocalDate desde, LocalDate hasta,
            String orden, Docente soloDeDocente);

    // ---------------------------------------------------------------- Creaciones

    /**
     * Registra una cohorte para un programa activo. Nace Activa si el contenido del programa está completo; si no, Pendiente.
     *
     * @param idPrograma el identificador del programa
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo; el fin se calcula)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Cohorte registrarCohorte(Integer idPrograma, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, Integer accesoDias, Integer cupoMaximo);

    /**
     * Registra una cohorte de un curso activo, con o sin programa. Sin programa (o con el contenido incompleto) la cohorte
     * nace Pendiente, y su inscripción debe empezar con el plazo mínimo para que el docente complete el contenido.
     *
     * @param idCurso el identificador del curso
     * @param idPrograma el identificador del programa (opcional; debe ser del curso)
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo; el fin se calcula)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Cohorte registrarCohorte(Integer idCurso, Integer idPrograma, LocalDate fechaInicioInscripcion,
            LocalDate fechaFinInscripcion, LocalDate fechaInicioDictado, Integer accesoDias,
            Integer cupoMaximo);

    /**
     * Devuelve qué le falta al contenido del programa para que una cohorte pueda estar Activa. Sin programa, falta elegirlo.
     *
     * @param programa el programa (puede ser {@code null})
     * @return los faltantes; vacío si el contenido está completo
     */
    List<String> faltantesDeContenido(Programa programa);

    /**
     * Días que como mínimo debe haber entre hoy y el inicio de la inscripción de una cohorte pendiente.
     *
     * @return el plazo mínimo en días
     */
    int plazoMinimoParaCompletarContenidoDias();

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Asigna el programa a una cohorte pendiente. Si el contenido del programa ya está completo, la cohorte pasa a Activa.
     *
     * @param idCohorte el identificador de la cohorte
     * @param idPrograma el identificador del programa (debe ser del curso de la cohorte)
     * @param docente el docente que asigna el programa (debe participar en el curso)
     * @return la cohorte con el programa asignado
     * @throws IllegalArgumentException si la cohorte no está pendiente o el programa no es válido
     */
    Cohorte asignarPrograma(Integer idCohorte, Integer idPrograma, Docente docente);

    /**
     * Modifica una cohorte activa sin inscripciones activas asociadas.
     *
     * @param idCohorte el identificador de la cohorte
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo; el fin se calcula)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Cohorte modificarCohorte(Integer idCohorte, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, Integer accesoDias, Integer cupoMaximo);

    /**
     * Cambia el contexto de trabajo de un docente: establece la cohorte (y su programa) como la de trabajo
     * por defecto en el curso al que pertenece.
     *
     * @param cohorte la cohorte sobre la que el docente va a trabajar
     * @param docente el docente que cambia de contexto
     * @throws IllegalArgumentException si el docente no participa en el curso de la cohorte
     */
    void cambiarContextoDeTrabajo(Cohorte cohorte, Docente docente);

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja una cohorte activa que no tenga inscripciones ni clases en vivo activas, y la desvincula
     * de los docentes que la tienen asignada por defecto.
     *
     * @param idCohorte el identificador de la cohorte
     * @throws IllegalArgumentException si la cohorte no está activa o tiene inscripciones o clases en vivo activas
     */
    void darDeBajaCohorte(Integer idCohorte);

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja ninguno.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    void darDeBajaVarios(java.util.List<Integer> ids);
}
