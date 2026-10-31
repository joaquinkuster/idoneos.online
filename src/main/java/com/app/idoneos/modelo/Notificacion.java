package com.app.idoneos.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notificación que el sistema le envía por correo a un usuario (por ejemplo, el aviso de que una cohorte pendiente se
 * activó o se canceló). Guarda el estado del envío y sus intentos.
 */
@Entity
@Table(name = "Notificacion")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificacion")
    private int idNotificacion;

    /**
     * Relación con Usuario: quien recibe la notificación.
     */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /**
     * Relación con TipoNotificacion.
     */
    @ManyToOne
    @JoinColumn(name = "idTipoNotificacion", nullable = false)
    private TipoNotificacion tipoNotificacion;

    /**
     * Relación con Cohorte: la cohorte a la que se refiere. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = true)
    private Cohorte cohorte;

    /**
     * Relación con EstadoEnvio.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoEnvio", nullable = false)
    private EstadoEnvio estadoEnvio;

    /**
     * Asunto del correo.
     */
    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    /**
     * Fecha y hora en que se envió. Es opcional (todavía no se envió).
     */
    @Column(name = "fechaEnvio", nullable = true)
    private LocalDateTime fechaEnvio;

    /**
     * Cantidad de intentos de envío.
     */
    @Column(name = "intentos", nullable = false)
    private int intentos = 0;

    /**
     * Detalle del error del último intento. Es opcional.
     */
    @Column(name = "detalleError", nullable = true, length = 500)
    private String detalleError;

    /**
     * Fecha y hora de creación.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación. Es opcional.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Constructor para crear la notificación con los datos básicos.
     *
     * @param usuario          quien la recibe.
     * @param tipoNotificacion el tipo.
     * @param cohorte          la cohorte a la que se refiere (opcional).
     * @param estadoEnvio      el estado del envío.
     * @param asunto           el asunto.
     */
    public Notificacion(Usuario usuario, TipoNotificacion tipoNotificacion, Cohorte cohorte, EstadoEnvio estadoEnvio,
            String asunto) {
        this.usuario = usuario;
        this.tipoNotificacion = tipoNotificacion;
        this.cohorte = cohorte;
        this.estadoEnvio = estadoEnvio;
        this.asunto = asunto;
    }

    /**
     * Devuelve una representación en forma de cadena.
     *
     * @return El asunto.
     */
    @Override
    public String toString() {
        return asunto;
    }
}
