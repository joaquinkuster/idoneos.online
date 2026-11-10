package com.app.idoneos.service.progreso;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.TemaCronograma;
import java.time.LocalDateTime;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Tema;
import java.util.List;
import java.util.Optional;

/** Servicio del progreso. */
public interface ProgresoService extends CrudService<Progreso> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros de progreso asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Progreso asociados al Tema indicado
     */
    List<Progreso> buscarPorTema(Tema tema);

    /**
     * Busca el progreso a partir de la combinación única de sus relaciones.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Progreso> buscarPorInscripcionYTema(Inscripcion inscripcion, Tema tema);

    /**
     * Busca las inscripciones de un curso con su progreso.
     *
     * @param cursoId El identificador del curso.
     * @param alumno Texto a buscar en el nombre, DNI o correo del alumno (opcional).
     * @return Las inscripciones que cumplen los criterios, de la más reciente a la más antigua.
     */
    List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno);

    /**
     * Arma el detalle del progreso de una inscripción: cada tema del programa de su cohorte.
     *
     * @param idInscripcion El identificador de la inscripción.
     * @return El detalle del progreso.
     * @throws IllegalArgumentException Si la inscripción no existe o fue dada de baja.
     */
    DetalleProgreso buscarDetalle(Integer idInscripcion);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Registra el tema como completada para un alumno si la aprobó.
     *
     * @param inscripcion la inscripción del alumno
     * @param tema el tema a verificar
     * @return {@code true} si el tema quedó completada con esta llamada
     */
    boolean actualizar(Inscripcion inscripcion, Tema tema);

    /**
     * Verifica el tema para cada alumno con inscripción vigente en un programa que la incluye.
     *
     * @param tema el tema cuyos autoevaluaciones cambiaron
     */
    void actualizarPorTema(Tema tema);

    /**
     * Revierte el tema a no completada para un alumno (por ejemplo, por fraude).
     *
     * @param inscripcion la inscripción del alumno
     * @param tema el tema a revertir
     * @return {@code true} si el tema estaba completada
     */
    boolean revertir(Inscripcion inscripcion, Tema tema);

    /**
     * Detalle del progreso de un alumno: cada tema del programa de su cohorte, si la completó y cuándo.
     *
     * @param inscripcion La inscripción del alumno.
     * @param temas Los temas del programa de la cohorte, en orden de dictado.
     */
    public record DetalleProgreso(Inscripcion inscripcion, List<AvanceTema> temas) {

        /**
         * Avance del alumno en un tema del programa.
         *
         * @param tema El tema del cronograma.
         * @param completada Si el alumno la completó.
         * @param fechaCompletada Cuándo la completó, {@code null} si no la completó o no se registró la fecha.
         */
        public record AvanceTema(TemaCronograma tema, boolean completada, LocalDateTime fechaCompletada) {
        }
    }
}
