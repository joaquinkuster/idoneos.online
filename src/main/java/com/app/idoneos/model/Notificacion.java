package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación in-app enviada a un usuario.
 */
@Entity
@Table(name = "notificaciones")
@Getter @Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "mensaje", nullable = false, length = 250)
    private String mensaje;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public Notificacion(String titulo, String mensaje, Usuario usuario) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }
}
