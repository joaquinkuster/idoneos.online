package com.app.idoneos.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una pregunta sorteada para un intento de autoevaluación. */
@Entity
@Table(name = "PreguntaIntento", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idIntentoAutoevaluacion", "idPregunta"}),
        @UniqueConstraint(columnNames = {"idIntentoAutoevaluacion", "orden"}) })
@Getter
@Setter
@NoArgsConstructor
public class PreguntaIntento {

    /** Identificador único de la pregunta del intento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPreguntaIntento")
    private int idPreguntaIntento;

    /** Intento autoevaluacion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idIntentoAutoevaluacion", nullable = false)
    private IntentoAutoevaluacion intentoAutoevaluacion;

    /** Pregunta de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPregunta", nullable = false)
    private Pregunta pregunta;

    /** Posición de la pregunta en el cuestionario, empezando en 1. */
    @Column(name = "orden", nullable = false)
    private int orden;

    /** Indica si el alumno marcó la pregunta para revisarla antes de entregar. */
    @Column(name = "marcada", nullable = false)
    private Boolean marcada = false;

    /** Indica si la pregunta ya fue pasada en un cuestionario secuencial, sin navegación libre. */
    public Boolean getPasada() {
        return marcada;
    }

    /** Indica que el alumno pasó por la pregunta en un cuestionario secuencial. */
    public void setPasada(Boolean pasada) {
        this.marcada = pasada;
    }

    /** Respuestas intento que referencian a este registro. */
    @OneToMany(mappedBy = "preguntaIntento", cascade = CascadeType.ALL)
    private Set<RespuestaIntento> respuestasIntento = new HashSet<>();

    /** Crea la pregunta del intento con los datos básicos. */
    public PreguntaIntento(IntentoAutoevaluacion intentoAutoevaluacion, Pregunta pregunta, int orden) {
        this.intentoAutoevaluacion = intentoAutoevaluacion;
        this.pregunta = pregunta;
        this.orden = orden;
    }

    /** Indica si el alumno eligió al menos una opción para la pregunta. */
    public boolean estaRespondida() {
        return !respuestasIntento.isEmpty();
    }

    /** Obtiene las opciones que el alumno eligió para la pregunta. */
    public List<OpcionRespuesta> getOpcionesElegidas() {
        return respuestasIntento.stream().map(RespuestaIntento::getOpcionRespuesta)
                .sorted(Comparator.comparingInt(OpcionRespuesta::getIdOpcionRespuesta)).toList();
    }

    /** Devuelve una representación en forma de cadena de la pregunta del intento. */
    @Override
    public String toString() {
        return "PreguntaIntento #" + idPreguntaIntento;
    }
}
