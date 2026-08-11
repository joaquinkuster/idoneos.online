package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intento_autoevaluacion")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentoAutoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "nota", nullable = false)
    private double nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoevaluacion_id", nullable = false)
    private Autoevaluacion autoevaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @OneToMany(mappedBy = "intentoAutoevaluacion", cascade = CascadeType.ALL)
    private List<RespuestaIntento> respuestasIntentos = new ArrayList<>();
}
