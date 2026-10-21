package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Opción elegida por el alumno para una pregunta de un intento de autoevaluación.
 */
@Entity
@Table(name = "RespuestaIntento", uniqueConstraints = { @UniqueConstraint(columnNames = {"idPreguntaIntento", "idOpcionRespuesta"}) })
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
     * Relación con PreguntaIntento.
     */
    @ManyToOne
    @JoinColumn(name = "idPreguntaIntento", nullable = false)
    private PreguntaIntento preguntaIntento;

    /**
     * Relación con OpcionRespuesta.
     */
    @ManyToOne
    @JoinColumn(name = "idOpcionRespuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    /**
     * Constructor para crear la respuesta del intento con los datos básicos.
     *
     * @param preguntaIntento relación con PreguntaIntento.
     * @param opcionRespuesta relación con OpcionRespuesta.
     */
    public RespuestaIntento(PreguntaIntento preguntaIntento, OpcionRespuesta opcionRespuesta) {
        this.preguntaIntento = preguntaIntento;
        this.opcionRespuesta = opcionRespuesta;
    }

    /**
     * Calcula cuánto suma o resta a la nota del intento la opción elegida.
     *
     * @param descuentoPorError Si la autoevaluación descuenta las respuestas incorrectas.
     * @param cantidadPreguntas La cantidad de preguntas de la autoevaluación.
     * @return El valor de la opción, sobre una nota de 0 a 1.
     */
    public double calcularValorOpcion(boolean descuentoPorError, int cantidadPreguntas) {
        return opcionRespuesta.calcularValorOpcion(descuentoPorError, cantidadPreguntas);
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
