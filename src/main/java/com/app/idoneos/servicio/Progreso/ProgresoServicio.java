package com.app.idoneos.servicio.Progreso;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con el progreso.
 */
public interface ProgresoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros de progreso asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Progreso asociados al Unidad indicado
     */
    List<Progreso> buscarPorUnidad(Unidad unidad);

    /**
     * Busca los registros de progreso asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Progreso asociados al Inscripcion indicado
     */
    List<Progreso> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Progreso> buscarPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad);

    /**
     * CU-55: Busca las inscripciones de un curso con su progreso. Solo incluye inscripciones activas (no dadas de baja),
     * de todas las cohortes del curso, y permite filtrar por alumno.
     *
     * @param cursoId El identificador del curso.
     * @param alumno  Texto a buscar en el nombre, DNI o correo del alumno (opcional).
     * @return Las inscripciones que cumplen los criterios, de la más reciente a la más antigua.
     */
    List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno);

    /**
     * CU-55: Arma el detalle del progreso de una inscripción: cada unidad del programa de su cohorte, si fue
     * completada y la fecha en que se completó.
     *
     * @param idInscripcion El identificador de la inscripción.
     * @return El detalle del progreso.
     * @throws IllegalArgumentException Si la inscripción no existe o fue dada de baja.
     */
    DetalleProgreso buscarDetalle(Integer idInscripcion);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Registra la unidad como completada para un alumno si la aprobó: tiene al menos una autoevaluación visible y aprobó
     * todas las que tiene visibles. Al completarla se habilita la siguiente unidad del cronograma y, si con ella el alumno
     * completó todas las unidades del programa, se emite su certificado de finalización. No hace nada si la unidad ya
     * estaba completada.
     *
     * @param inscripcion la inscripción del alumno
     * @param unidad la unidad a verificar
     * @return {@code true} si la unidad quedó completada con esta llamada
     */
    boolean actualizarProgreso(Inscripcion inscripcion, Unidad unidad);

    /**
     * Verifica la unidad para cada alumno con inscripción vigente en un programa que la incluye: completa la de quienes ya
     * aprobaron todas las autoevaluaciones visibles que le quedan (por ejemplo, después de ocultar o dar de baja una).
     *
     * @param unidad la unidad cuyas autoevaluaciones cambiaron
     */
    void actualizarProgresoDeLaUnidad(Unidad unidad);

    /**
     * Revierte la unidad a no completada para un alumno (por ejemplo, por fraude). Las unidades posteriores ya completadas
     * conservan su estado pero quedan bloqueadas hasta que el alumno vuelva a completar esta. Si el alumno tenía un
     * certificado vigente, lo anula, porque el curso deja de estar completo.
     *
     * @param inscripcion la inscripción del alumno
     * @param unidad la unidad a revertir
     * @return {@code true} si la unidad estaba completada
     */
    boolean revertirProgreso(Inscripcion inscripcion, Unidad unidad);
}
