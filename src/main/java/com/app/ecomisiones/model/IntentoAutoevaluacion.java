package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra un intento de un alumno sobre una autoevaluación.
 * Cada intento tiene 10 preguntas sorteadas del pool.
 */
@Entity
@Table(name = "intentos_autoevaluacion")
@Getter @Setter
@NoArgsConstructor
public class IntentoAutoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "nota", nullable = true)
    private Double nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespuestaIntento> respuestas = new ArrayList<>();

    public IntentoAutoevaluacion(Autoevaluacion autoevaluacion, Usuario usuario) {
        this.autoevaluacion = autoevaluacion;
        this.usuario = usuario;
    }

    public boolean isAprobado(double umbral) {
        return nota != null && nota >= umbral;
    }
}
