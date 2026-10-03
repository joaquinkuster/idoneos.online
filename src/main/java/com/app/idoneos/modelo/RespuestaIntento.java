package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Opción elegida por el alumno en un intento de autoevaluación.
 */
@Entity
@Table(name = "RespuestaIntento", uniqueConstraints = { @UniqueConstraint(columnNames = {"idIntentoAutoevaluacion", "idOpcionRespuesta"}) })
@Getter
@Setter
@NoArgsConstructor
public class RespuestaIntento {

    /**
     * Identificador único de la respuesta del intento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRespuestaIntento")
    private int idRespuestaIntento;

    /**
     * Relación con IntentoAutoevaluacion.
     */
    @ManyToOne
    @JoinColumn(name = "idIntentoAutoevaluacion", nullable = false)
    private IntentoAutoevaluacion intentoAutoevaluacion;

    /**
     * Relación con OpcionRespuesta.
     */
    @ManyToOne
    @JoinColumn(name = "idOpcionRespuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    /**
     * Constructor para crear la respuesta del intento con los datos básicos.
     *
     * @param intentoAutoevaluacion relación con IntentoAutoevaluacion.
     * @param opcionRespuesta relación con OpcionRespuesta.
     */
    public RespuestaIntento(IntentoAutoevaluacion intentoAutoevaluacion, OpcionRespuesta opcionRespuesta) {
        this.intentoAutoevaluacion = intentoAutoevaluacion;
        this.opcionRespuesta = opcionRespuesta;
    }

    /**
     * Devuelve una representación en forma de cadena de la respuesta del intento.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "RespuestaIntento #" + idRespuestaIntento;
    }
}
