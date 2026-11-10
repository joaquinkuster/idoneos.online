package com.app.idoneos.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una notificación que el sistema envía por correo a un usuario. */
@Entity
@Table(name = "Notificacion")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificacion")
    private int idNotificacion;

    /** Relación con Usuario: quien recibe la notificación. */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /** Tipo notificacion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idTipoNotificacion", nullable = false)
    private TipoNotificacion tipoNotificacion;

    /** Relación con Cohorte: la cohorte a la que se refiere. */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = true)
    private Cohorte cohorte;

    /** Estado envio de este registro. */
    @ManyToOne
    @JoinColumn(name = "idEstadoEnvio", nullable = false)
    private EstadoEnvio estadoEnvio;

    /** Asunto del correo. */
    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    /** Fecha y hora en que se envió. */
    @Column(name = "fechaEnvio", nullable = true)
    private LocalDateTime fechaEnvio;

    /** Cantidad de intentos de envío. */
    @Column(name = "intentos", nullable = false)
    private int intentos = 0;

    /** Detalle del error del último intento. */
    @Column(name = "detalleError", nullable = true, length = 500)
    private String detalleError;

    /** Fecha y hora de creación. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Crea la notificación con los datos básicos. */
    public Notificacion(Usuario usuario, TipoNotificacion tipoNotificacion, Cohorte cohorte, EstadoEnvio estadoEnvio,
            String asunto) {
        this.usuario = usuario;
        this.tipoNotificacion = tipoNotificacion;
        this.cohorte = cohorte;
        this.estadoEnvio = estadoEnvio;
        this.asunto = asunto;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return asunto;
    }
}
