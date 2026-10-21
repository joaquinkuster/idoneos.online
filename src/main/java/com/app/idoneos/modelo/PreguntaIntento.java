package com.app.idoneos.modelo;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pregunta sorteada para un intento de autoevaluación, con su posición en el cuestionario y la marca de revisión del alumno.
 */
@Entity
@Table(name = "PreguntaIntento", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idIntentoAutoevaluacion", "idPregunta"}),
        @UniqueConstraint(columnNames = {"idIntentoAutoevaluacion", "orden"}) })
@Getter
@Setter
@NoArgsConstructor
public class PreguntaIntento {

    /**
     * Identificador único de la pregunta del intento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPreguntaIntento")
    private int idPreguntaIntento;

    /**
     * Relación con IntentoAutoevaluacion.
     */
    @ManyToOne
    @JoinColumn(name = "idIntentoAutoevaluacion", nullable = false)
    private IntentoAutoevaluacion intentoAutoevaluacion;

    /**
     * Relación con Pregunta.
     */
    @ManyToOne
    @JoinColumn(name = "idPregunta", nullable = false)
    private Pregunta pregunta;

    /**
     * Posición de la pregunta en el cuestionario, empezando en 1.
     */
    @Column(name = "orden", nullable = false)
    private int orden;

    /**
     * Indica si el alumno marcó la pregunta para revisarla antes de entregar.
     */
    @Column(name = "marcada", nullable = false)
    private Boolean marcada = false;

    /**
     * En un cuestionario secuencial (sin navegación libre), indica que el alumno ya pasó por la pregunta (respondida o no) y
     * no puede volver a ella. Con navegación libre no se usa.
     */
    @Column(name = "pasada", nullable = false)
    private Boolean pasada = false;

    /**
     * Conjunto de respuestasIntento asociados.
     */
    @OneToMany(mappedBy = "preguntaIntento", cascade = CascadeType.ALL)
    private Set<RespuestaIntento> respuestasIntento = new HashSet<>();

    /**
     * Constructor para crear la pregunta del intento con los datos básicos.
     *
     * @param intentoAutoevaluacion relación con IntentoAutoevaluacion.
     * @param pregunta relación con Pregunta.
     * @param orden posición de la pregunta en el cuestionario.
     */
    public PreguntaIntento(IntentoAutoevaluacion intentoAutoevaluacion, Pregunta pregunta, int orden) {
        this.intentoAutoevaluacion = intentoAutoevaluacion;
        this.pregunta = pregunta;
        this.orden = orden;
    }

    /**
     * Indica si el alumno eligió al menos una opción para la pregunta.
     *
     * @return {@code true} si la pregunta tiene respuestas.
     */
    public boolean estaRespondida() {
        return !respuestasIntento.isEmpty();
    }

    /**
     * Obtiene las opciones que el alumno eligió para la pregunta.
     *
     * @return Las opciones elegidas, en el orden en que se cargaron.
     */
    public List<OpcionRespuesta> getOpcionesElegidas() {
        return respuestasIntento.stream().map(RespuestaIntento::getOpcionRespuesta)
                .sorted(Comparator.comparingInt(OpcionRespuesta::getIdOpcionRespuesta)).toList();
    }

    /**
     * Devuelve una representación en forma de cadena de la pregunta del intento.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "PreguntaIntento #" + idPreguntaIntento;
    }
}
