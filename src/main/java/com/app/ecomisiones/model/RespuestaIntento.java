package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Registra la opción elegida por el alumno para cada pregunta de un intento.
 * Sin atributos propios más allá de sus claves foráneas.
 * No referencia a Pregunta directamente — se llega a ella vía OpcionRespuesta.
 */
@Entity
@Table(name = "respuestas_intento")
@Getter @Setter
@NoArgsConstructor
public class RespuestaIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intento", nullable = false)
    private IntentoAutoevaluacion intento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_respuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    public RespuestaIntento(IntentoAutoevaluacion intento, OpcionRespuesta opcionRespuesta) {
        this.intento = intento;
        this.opcionRespuesta = opcionRespuesta;
    }
}
