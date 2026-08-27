package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Sesion: Registro de inicios y cierres de sesión de un usuario.
 * Mapea directamente a la tabla "Sesion" en base_datos.sql.
 */
@Entity
@Table(name = "Sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private int idSesion;

    /** Token único de la sesión activa. */
    @Column(name = "token", nullable = false, length = 255)
    private String token;

    /** Fecha y hora de inicio de la sesión. */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    /** Fecha y hora de cierre de la sesión. */
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /** Dirección IP de conexión (soporta IPv6). */
    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    /** Descripción del dispositivo o navegador utilizado. */
    @Column(name = "dispositivo", nullable = false, length = 255)
    private String dispositivo;

    /** Usuario al que corresponde la sesión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public Sesion() {
    }

    public int getId() {
        return idSesion;
    }

    public int getIdSesion() {
        return idSesion;
    }

    public void setId(int id) {
        this.idSesion = id;
    }

    public void setIdSesion(int idSesion) {
        this.idSesion = idSesion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}

