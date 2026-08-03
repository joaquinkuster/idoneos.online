package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registra cada inicio de sesión de un usuario para trazabilidad de seguridad.
 */
@Entity
@Table(name = "sesiones")
@Getter @Setter
@NoArgsConstructor
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    /**
     * Null mientras la sesión está activa.
     */
    @Column(name = "fecha_fin", nullable = true)
    private LocalDateTime fechaFin;

    /**
     * Longitud 45 para contemplar IPv6.
     */
    @Column(name = "ip", nullable = true, length = 45)
    private String ip;

    @Column(name = "dispositivo", nullable = true, length = 255)
    private String dispositivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public Sesion(String token, String ip, String dispositivo, Usuario usuario) {
        this.token = token;
        this.ip = ip;
        this.dispositivo = dispositivo;
        this.usuario = usuario;
    }

    public boolean estaActiva() {
        return fechaFin == null;
    }
}
