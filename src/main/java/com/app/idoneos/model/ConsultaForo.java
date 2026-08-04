package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Pregunta de un alumno dentro de una unidad.
 * Foro por unidad (no uno general por curso) para mantener consultas agrupadas por tema.
 */
@Entity
@Table(name = "consultas_foro")
@Getter @Setter
@NoArgsConstructor
public class ConsultaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha = LocalDate.now();

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public ConsultaForo(String texto, Unidad unidad, Usuario usuario) {
        this.texto = texto;
        this.unidad = unidad;
        this.usuario = usuario;
    }
}
