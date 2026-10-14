package com.app.idoneos.servicio.Progreso;

import java.time.LocalDateTime;
import java.util.List;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.UnidadCronograma;

/**
 * Detalle del progreso de un alumno (CU-55): cada unidad del programa de su cohorte, si la completó y cuándo.
 * Lo arma {@link ProgresoServicio} y la plantilla lo recibe como atributo del modelo.
 *
 * @param inscripcion La inscripción del alumno.
 * @param unidades    Las unidades del programa de la cohorte, en orden de dictado.
 */
public record DetalleProgreso(Inscripcion inscripcion, List<AvanceUnidad> unidades) {

    /**
     * Avance del alumno en una unidad del programa.
     *
     * @param unidad          La unidad del cronograma.
     * @param completada      Si el alumno la completó.
     * @param fechaCompletada Cuándo la completó; {@code null} si no la completó o no se registró la fecha.
     */
    public record AvanceUnidad(UnidadCronograma unidad, boolean completada, LocalDateTime fechaCompletada) {
    }
}
