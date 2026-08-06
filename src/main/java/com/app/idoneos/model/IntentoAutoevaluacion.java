package com.app.idoneos.model;

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
@Table(name = "intento_autoevaluacion")
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

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public Autoevaluacion getAutoevaluacion() { return autoevaluacion; }
    public void setAutoevaluacion(Autoevaluacion autoevaluacion) { this.autoevaluacion = autoevaluacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<RespuestaIntento> getRespuestas() { return respuestas; }
    public void setRespuestas(List<RespuestaIntento> respuestas) { this.respuestas = respuestas; }

    public IntentoAutoevaluacion(Autoevaluacion autoevaluacion, Usuario usuario) {
        this.autoevaluacion = autoevaluacion;
        this.usuario = usuario;
    }

    public boolean isAprobado(double umbral) {
        return nota != null && nota >= umbral;
    }
}
