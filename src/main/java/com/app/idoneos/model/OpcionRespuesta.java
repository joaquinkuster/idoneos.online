package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Opción de respuesta de una pregunta de evaluación.
 */
@Entity
@Table(name = "opciones_respuesta")
@Getter @Setter
@NoArgsConstructor
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /**
     * Marca si esta opción es la respuesta correcta.
     */
    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta = false;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    public OpcionRespuesta(String texto, Boolean esCorrecta, Pregunta pregunta) {
        this.texto = texto;
        this.esCorrecta = esCorrecta;
        this.pregunta = pregunta;
    }
}
