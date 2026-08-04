package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Respuesta de un docente (titular o supervisor) a una ConsultaForo.
 */
@Entity
@Table(name = "respuesta_foro")
@Getter @Setter
@NoArgsConstructor
public class RespuestaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta", nullable = false)
    private ConsultaForo consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    public RespuestaForo(String texto, ConsultaForo consulta, Docente docente) {
        this.texto = texto;
        this.consulta = consulta;
        this.docente = docente;
    }
}
