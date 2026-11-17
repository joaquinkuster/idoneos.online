package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un tipo de notificación que el sistema envía por correo. */
@Entity
@Table(name = "TipoNotificacion")
@Getter
@Setter
@NoArgsConstructor
public class TipoNotificacion {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoNotificacion")
    private int idTipoNotificacion;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el registro con los datos básicos. */
    public TipoNotificacion(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Aviso de que una cohorte pendiente de contenido pasó a inscripción próxima. */
    public static final String COHORTE_ACTIVADA = "Cohorte activada";

    /** Aviso de que una cohorte pendiente se canceló. */
    public static final String COHORTE_CANCELADA = "Cohorte cancelada";

    /** Aviso que recibe el alumno que marcó "Recordarme" en una cohorte próxima. */
    public static final String RECORDATORIO_COHORTE = "Recordatorio de cohorte";

    /** Aviso al docente de que un alumno hizo una consulta en el foro. */
    public static final String CONSULTA_FORO = "Nueva consulta de foro";

    /** Aviso al alumno de que su consulta del foro fue respondida. */
    public static final String RESPUESTA_FORO = "Respuesta de foro";

    /** Aviso al alumno de que su pago fue aprobado. */
    public static final String PAGO_APROBADO = "Pago aprobado";

    /** Aviso al alumno de que su pago fue rechazado o no se confirmó. */
    public static final String PAGO_RECHAZADO = "Pago rechazado";

    /** Aviso al alumno de que se emitió su certificado. */
    public static final String CERTIFICADO_EMITIDO = "Certificado emitido";

    /** Aviso al alumno de que un docente aprobó su intento de autoevaluación. */
    public static final String INTENTO_APROBADO = "Intento aprobado";

    /** Aviso al alumno de que se anuló su intento de autoevaluación. */
    public static final String INTENTO_ANULADO = "Intento anulado";

    /** Aviso a los alumnos de la cohorte de que se programó una clase en vivo. */
    public static final String CLASE_PROGRAMADA = "Clase en vivo programada";

    /** Aviso a los alumnos de la cohorte de que cambió la fecha de una clase en vivo. */
    public static final String CLASE_MODIFICADA = "Clase en vivo modificada";

    /** Aviso a los alumnos de la cohorte de que se canceló una clase en vivo. */
    public static final String CLASE_CANCELADA = "Clase en vivo cancelada";

    /** Aviso a los alumnos de la cohorte de que la grabación de una clase ya está disponible. */
    public static final String GRABACION_DISPONIBLE = "Grabación disponible";

    /** Aviso a los alumnos de la cohorte de que una grabación está por vencer. */
    public static final String GRABACION_POR_VENCER = "Grabación por vencer";

    /** Aviso al docente de que un material generado está listo para su revisión. */
    public static final String MATERIAL_PARA_REVISAR = "Material para revisar";

    /** Aviso al docente de que falló la generación de un material y puede reintentar. */
    public static final String GENERACION_FALLIDA = "Generación fallida";

    /** Aviso al docente de que fue habilitado o suspendido para dictar clases. */
    public static final String HABILITACION_DOCENTE = "Habilitación de docente";
}
