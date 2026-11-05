package com.app.idoneos.service.recordatoriocohorte;

import com.app.idoneos.model.Alumno;
import java.util.Set;

/**
 * Recordatorios de cohortes próximas: el alumno marca «Recordarme» y, cuando faltan los días de anticipación del parámetro para
 * que abra la inscripción, el sistema le crea una notificación.
 */
public interface RecordatorioCohorteService {

    /**
     * Marca una cohorte próxima para que el sistema le avise al alumno (o reactiva el recordatorio que había quitado).
     *
     * @param alumno    el alumno que pide el recordatorio
     * @param idCohorte el identificador de la cohorte
     * @throws IllegalArgumentException si el alumno no es válido o la cohorte no está activa ni es próxima
     */
    void recordar(Alumno alumno, Integer idCohorte);

    /**
     * Quita el recordatorio de una cohorte (baja lógica).
     *
     * @param alumno    el alumno dueño del recordatorio
     * @param idCohorte el identificador de la cohorte
     * @throws IllegalArgumentException si el alumno no tiene un recordatorio vigente para esa cohorte
     */
    void quitar(Alumno alumno, Integer idCohorte);

    /**
     * Identificadores de las cohortes que el alumno tiene marcadas con un recordatorio vigente.
     *
     * @param alumno el alumno, o {@code null} si es un visitante
     * @return los identificadores (vacío si no hay)
     */
    Set<Integer> idsDeCohortesRecordadas(Alumno alumno);

    /**
     * Crea la notificación «Recordatorio de cohorte» de los recordatorios cuya cohorte abre su inscripción dentro de los días de
     * anticipación del parámetro. Cada recordatorio avisa una sola vez.
     *
     * @return la cantidad de notificaciones creadas
     */
    int crearNotificacionesPendientes();
}
