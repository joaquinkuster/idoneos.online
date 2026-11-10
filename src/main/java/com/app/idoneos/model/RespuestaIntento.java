package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la opción elegida por el alumno para una pregunta de un intento de autoevaluación. */
@Entity
@Table(name = "RespuestaIntento", uniqueConstraints = { @UniqueConstraint(columnNames = {"idPreguntaIntento", "idOpcionRespuesta"}) })
@Getter
@Setter
@NoArgsConstructor
public class RespuestaIntento {

    /** Identificador único de la respuesta del intento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRespuestaIntento")
    private int idRespuestaIntento;

    /** Pregunta intento de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPreguntaIntento", nullable = false)
    private PreguntaIntento preguntaIntento;

    /** Opcion respuesta de este registro. */
    @ManyToOne
    @JoinColumn(name = "idOpcionRespuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

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
        return "RespuestaIntento #" + idRespuestaIntento;
    }
}
