package com.app.idoneos.servicio.AccesoCurso;

import java.util.Comparator;
import java.util.List;

import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;

/**
 * Autoevaluación de una unidad junto con los intentos que el alumno ya registró sobre ella (CU-27).
 *
 * @param autoevaluacion La autoevaluación publicada.
 * @param intentos       Los intentos propios del alumno, del más antiguo al más reciente.
 */
public record AutoevaluacionAcceso(Autoevaluacion autoevaluacion, List<IntentoAutoevaluacion> intentos) {

    /**
     * Cantidad de intentos que el alumno ya usó.
     *
     * @return La cantidad de intentos registrados.
     */
    public int intentosUsados() {
        return intentos.size();
    }

    /**
     * Cantidad de intentos que le quedan al alumno.
     *
     * @return Los intentos restantes, o {@code null} si la autoevaluación no limita los intentos.
     */
    public Integer intentosRestantes() {
        Integer permitidos = autoevaluacion.getIntentosPermitidos();
        return permitidos == null ? null : Math.max(0, permitidos - intentos.size());
    }

    /**
     * Mejor nota obtenida entre los intentos entregados.
     *
     * @return La mejor nota, o {@code null} si el alumno todavía no entregó ningún intento.
     */
    public Float mejorNota() {
        return intentos.stream().map(IntentoAutoevaluacion::getNota).filter(nota -> nota != null)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Indica si el alumno aprobó la autoevaluación en alguno de sus intentos.
     *
     * @return {@code true} si algún intento está aprobado.
     */
    public boolean aprobada() {
        return intentos.stream().anyMatch(intento -> Boolean.TRUE.equals(intento.getAprobado()));
    }
}
