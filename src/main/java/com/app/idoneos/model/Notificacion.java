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

    /** Estado del envío por correo. Vacío si la notificación solo aparece en la bandeja del sistema. */
    @ManyToOne
    @JoinColumn(name = "idEstadoEnvio", nullable = true)
    private EstadoEnvio estadoEnvio;

    /** Asunto del correo. */
    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    /** Texto del aviso, tal como se muestra en la bandeja. */
    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    /** Dirección del sistema a la que lleva el aviso al hacer clic. */
    @Column(name = "url", nullable = true, length = 150)
    private String url;

    /** Fecha y hora en que el usuario leyó el aviso. Vacía mientras no lo leyó. */
    @Column(name = "fechaLectura", nullable = true)
    private LocalDateTime fechaLectura;

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

    /** Indica si el usuario ya leyó el aviso. */
    public boolean estaLeida() {
        return fechaLectura != null;
    }

    /** Marca el aviso como leído, si todavía no lo estaba. */
    public void marcarLeida() {
        if (fechaLectura == null) {
            fechaLectura = LocalDateTime.now();
        }
    }

    /** Obtiene cuánto hace que se creó el aviso, con palabras ("hace 2 horas"). */
    public String getTiempoRelativo() {
        java.time.Duration pasado = java.time.Duration.between(fechaCreacion, LocalDateTime.now());
        long minutos = Math.max(0, pasado.toMinutes());
        if (minutos < 1) {
            return "hace un momento";
        }
        if (minutos < 60) {
            return "hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        }
        long horas = pasado.toHours();
        if (horas < 24) {
            return "hace " + horas + (horas == 1 ? " hora" : " horas");
        }
        long dias = pasado.toDays();
        if (dias < 30) {
            return "hace " + dias + (dias == 1 ? " día" : " días");
        }
        return "el " + fechaCreacion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Obtiene el ícono (de FontAwesome) que representa al tipo de aviso. */
    public String getIcono() {
        return switch (tipoNotificacion.getNombre()) {
            case TipoNotificacion.PAGO_APROBADO -> "fa-circle-check";
            case TipoNotificacion.PAGO_RECHAZADO -> "fa-circle-xmark";
            case TipoNotificacion.CERTIFICADO_EMITIDO -> "fa-award";
            case TipoNotificacion.INTENTO_APROBADO -> "fa-clipboard-check";
            case TipoNotificacion.INTENTO_ANULADO -> "fa-clipboard-question";
            case TipoNotificacion.CONSULTA_FORO, TipoNotificacion.RESPUESTA_FORO -> "fa-comments";
            case TipoNotificacion.CLASE_PROGRAMADA, TipoNotificacion.CLASE_MODIFICADA, TipoNotificacion.CLASE_CANCELADA -> "fa-video";
            case TipoNotificacion.GRABACION_DISPONIBLE, TipoNotificacion.GRABACION_POR_VENCER -> "fa-film";
            case TipoNotificacion.COHORTE_ACTIVADA, TipoNotificacion.COHORTE_CANCELADA, TipoNotificacion.RECORDATORIO_COHORTE -> "fa-calendar-days";
            case TipoNotificacion.MATERIAL_PARA_REVISAR, TipoNotificacion.GENERACION_FALLIDA -> "fa-wand-magic-sparkles";
            case TipoNotificacion.HABILITACION_DOCENTE -> "fa-chalkboard-user";
            default -> "fa-bell";
        };
    }
}
