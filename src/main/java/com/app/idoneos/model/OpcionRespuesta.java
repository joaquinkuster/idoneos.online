package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opcion_respuesta")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta = false;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @OneToMany(mappedBy = "opcionRespuesta", cascade = CascadeType.ALL)
    private List<RespuestaIntento> respuestasIntentos = new ArrayList<>();
}
