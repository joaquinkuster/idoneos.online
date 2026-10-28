package com.app.idoneos.servicio.Progreso;

import java.time.LocalDateTime;
import java.util.List;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.TemaCronograma;

/**
 * Detalle del progreso de un alumno (CU-55): cada tema del programa de su cohorte, si la completó y cuándo.
 * Lo arma {@link ProgresoServicio} y la plantilla lo recibe como atributo del modelo.
 *
 * @param inscripcion La inscripción del alumno.
 * @param temas    Los temas del programa de la cohorte, en orden de dictado.
 */
public record DetalleProgreso(Inscripcion inscripcion, List<AvanceTema> temas) {

    /**
     * Avance del alumno en un tema del programa.
     *
     * @param tema          El tema del cronograma.
     * @param completada      Si el alumno la completó.
     * @param fechaCompletada Cuándo la completó; {@code null} si no la completó o no se registró la fecha.
     */
    public record AvanceTema(TemaCronograma tema, boolean completada, LocalDateTime fechaCompletada) {
    }
}
