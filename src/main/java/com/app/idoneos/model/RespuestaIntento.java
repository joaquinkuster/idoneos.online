package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad RespuestaIntento: Registro de la opción seleccionada por el alumno
 * para una pregunta en un IntentoAutoevaluacion.
 * Mapea directamente a la tabla "RespuestaIntento" en base_datos.sql.
 */
@Entity
@Table(name = "RespuestaIntento")
public class RespuestaIntento {

    /** Identificador único de la respuesta enviada en el intento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Intento de autoevaluación al que pertenece la respuesta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_autoevaluacion_id")
    private IntentoAutoevaluacion intentoAutoevaluacion;

    /** Opción elegida por el alumno para la pregunta correspondida. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_respuesta_id")
    private OpcionRespuesta opcionRespuesta;

    public RespuestaIntento() {
    }

    public RespuestaIntento(IntentoAutoevaluacion intento, OpcionRespuesta opcion) {
        this.intentoAutoevaluacion = intento;
        this.opcionRespuesta = opcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IntentoAutoevaluacion getIntentoAutoevaluacion() {
        return intentoAutoevaluacion;
    }

    public void setIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion) {
        this.intentoAutoevaluacion = intentoAutoevaluacion;
    }

    public OpcionRespuesta getOpcionRespuesta() {
        return opcionRespuesta;
    }

    public void setOpcionRespuesta(OpcionRespuesta opcionRespuesta) {
        this.opcionRespuesta = opcionRespuesta;
    }
}
