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
}
