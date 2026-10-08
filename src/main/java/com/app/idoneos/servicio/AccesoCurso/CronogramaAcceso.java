package com.app.idoneos.servicio.AccesoCurso;

import java.time.LocalDateTime;
import java.util.List;

import com.app.idoneos.modelo.UnidadCronograma;

/**
 * Cronograma del programa de la cohorte del alumno, con el avance esperado y el indicador de atraso (CU-27).
 *
 * @param filas            Las unidades del cronograma en orden.
 * @param fechaBase        Fecha desde la que se cuentan las semanas (inicio de dictado o de inscripción de la cohorte).
 * @param desdeDictado     {@code true} si la fecha base es el inicio de dictado; {@code false} si es el inicio de la inscripción.
 * @param semanasTotales   Duración total del programa en semanas.
 * @param semanaEsperada   Semana en la que debería estar el alumno hoy (entre 1 y la duración total).
 * @param atrasado         Indica si el alumno va por detrás de lo esperado.
 * @param mensajeAtraso    Explicación del atraso, o {@code null} si el alumno está al día.
 */
public record CronogramaAcceso(List<FilaCronograma> filas, LocalDateTime fechaBase, boolean desdeDictado,
        int semanasTotales, int semanaEsperada, boolean atrasado, String mensajeAtraso) {

    /**
     * Unidad del cronograma con las semanas que le corresponden y su estado.
     *
     * @param cronograma La unidad en el cronograma (título, orden y duración).
     * @param semanaDesde Primera semana de la unidad.
     * @param semanaHasta Última semana de la unidad.
     * @param completada Indica si el alumno completó la unidad.
     * @param estado Estado para mostrar: Completada, Atrasada, Semana actual o Próxima.
     */
    public record FilaCronograma(UnidadCronograma cronograma, int semanaDesde, int semanaHasta, boolean completada,
            String estado) {
    }
}
