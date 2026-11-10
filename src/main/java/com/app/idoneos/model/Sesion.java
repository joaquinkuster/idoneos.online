package com.app.idoneos.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una sesión de acceso de un usuario, con su token, dispositivo e IP. */
@Entity
@Table(name = "Sesion")
@Getter
@Setter
@NoArgsConstructor
public class Sesion {

    /** Identificador único de la sesión. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSesion")
    private int idSesion;

    /** Usuario de este registro. */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /** Token de la sesión. */
    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    /** Fecha y hora de inicio de la sesión. */
    @Column(name = "fechaInicio", nullable = false)
    private LocalDateTime fechaInicio;

    /** Fecha y hora de fin de la sesión. */
    @Column(name = "fechaFin", nullable = false)
    private LocalDateTime fechaFin;

    /** Dirección IP desde la que se inició la sesión. */
    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    /** Dispositivo desde el que se inició la sesión. */
    @Column(name = "dispositivo", nullable = false, length = 255)
    private String dispositivo;

    /** Crea la sesión con los datos básicos. */
    public Sesion(Usuario usuario, String token, LocalDateTime fechaInicio, LocalDateTime fechaFin, String ip, String dispositivo) {
        this.usuario = usuario;
        this.token = token;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.ip = ip;
        this.dispositivo = dispositivo;
    }

    /** Devuelve una representación en forma de cadena de la sesión. */
    @Override
    public String toString() {
        return "Sesion #" + idSesion;
    }
}
