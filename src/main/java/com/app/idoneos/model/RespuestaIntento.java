package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "respuesta_intento")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_autoevaluacion_id", nullable = false)
    private IntentoAutoevaluacion intentoAutoevaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_respuesta_id", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public IntentoAutoevaluacion getIntentoAutoevaluacion() { return intentoAutoevaluacion; }
    public void setIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion) { this.intentoAutoevaluacion = intentoAutoevaluacion; }

    public OpcionRespuesta getOpcionRespuesta() { return opcionRespuesta; }
    public void setOpcionRespuesta(OpcionRespuesta opcionRespuesta) { this.opcionRespuesta = opcionRespuesta; }


    public RespuestaIntento(IntentoAutoevaluacion intentoAutoevaluacion, OpcionRespuesta opcionRespuesta) {
        this.intentoAutoevaluacion = intentoAutoevaluacion;
        this.opcionRespuesta = opcionRespuesta;
    }

}
