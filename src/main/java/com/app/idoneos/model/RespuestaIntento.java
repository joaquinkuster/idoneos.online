package com.app.idoneos.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la opción elegida por el alumno para una pregunta de un intento de autoevaluación. */
@Entity
@Table(name = "RespuestaIntento")
@IdClass(RespuestaIntento.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class RespuestaIntento {

    /** Clave primaria compuesta de la respuesta de un intento. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int opcionRespuesta;
        private PreguntaIntento.Clave preguntaIntento;
    }

    /** Opcion respuesta de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idOpcionRespuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    /** Pregunta intento de este registro. */
    @Id
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_intento_autoevaluacion", referencedColumnName = "id_intento_autoevaluacion", nullable = false),
            @JoinColumn(name = "id_pregunta", referencedColumnName = "id_pregunta", nullable = false) })
    private PreguntaIntento preguntaIntento;

    /** Crea la respuesta del intento con los datos básicos. */
    public RespuestaIntento(PreguntaIntento preguntaIntento, OpcionRespuesta opcionRespuesta) {
        this.preguntaIntento = preguntaIntento;
        this.opcionRespuesta = opcionRespuesta;
    }

    /** Calcula cuánto suma o resta a la nota del intento la opción elegida. */
    public double calcularValorOpcion(boolean descuentoPorError, int cantidadPreguntas) {
        return opcionRespuesta.calcularValorOpcion(descuentoPorError, cantidadPreguntas);
    }

    /** Devuelve una representación en forma de cadena de la respuesta del intento. */
    @Override
    public String toString() {
        return "RespuestaIntento #" + preguntaIntento + "-" + opcionRespuesta.getIdOpcionRespuesta();
    }
}
