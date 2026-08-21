package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad RespuestaIntento: Registro de la opción seleccionada en un intento de autoevaluación.
 * Mapea directamente a la tabla "RespuestaIntento" en base_datos.sql.
 */
@Entity
@Table(name = "RespuestaIntento")
public class RespuestaIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta_intento")
    private int idRespuestaIntento;

    /** Intento de autoevaluación al que pertenece la respuesta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intento_autoevaluacion", nullable = false)
    private IntentoAutoevaluacion intentoAutoevaluacion;

    /** Opción elegida por el alumno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_respuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    public RespuestaIntento() {
    }

    public RespuestaIntento(IntentoAutoevaluacion intento, OpcionRespuesta opcion) {
        this.intentoAutoevaluacion = intento;
        this.opcionRespuesta = opcion;
    }

    public int getId() {
        return idRespuestaIntento;
    }

    public int getIdRespuestaIntento() {
        return idRespuestaIntento;
    }

    public void setId(int id) {
        this.idRespuestaIntento = id;
    }

    public void setIdRespuestaIntento(int idRespuestaIntento) {
        this.idRespuestaIntento = idRespuestaIntento;
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

