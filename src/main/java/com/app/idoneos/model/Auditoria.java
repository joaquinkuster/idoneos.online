package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Auditoria: Registro de acciones realizadas sobre el sistema por los usuarios.
 * Mapea directamente a la tabla "Auditoria" en base_datos.sql.
 */
@Entity
@Table(name = "Auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private int idAuditoria;

    /** Nombre de la entidad afectada por la acción. */
    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    /** Identificador del registro afectado. */
    @Column(name = "id_afectado", nullable = false)
    private int idAfectado;

    /** Valor anterior al cambio (serializado como texto). */
    @Column(name = "valor_anterior", columnDefinition = "text")
    private String valorAnterior;

    /** Nuevo valor luego del cambio (serializado como texto). */
    @Column(name = "valor_nuevo", columnDefinition = "text")
    private String valorNuevo;

    /** Dirección IP del usuario que realizó la acción. */
    @Column(name = "ip_usuario", nullable = false, length = 45)
    private String ipUsuario;

    /** Fecha y hora exacta de la acción auditada. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    /** Tipo de acción realizada (Crear, Modificar, Eliminar, Consultar). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_accion_auditoria", nullable = false)
    private TipoAccionAuditoria tipoAccionAuditoria;

    /** Usuario que realizó la acción. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public Auditoria() {
    }

    public Auditoria(String entidadAfectada, int idAfectado, Usuario usuario, TipoAccionAuditoria tipoAccionAuditoria) {
        this.entidadAfectada = entidadAfectada;
        this.idAfectado = idAfectado;
        this.usuario = usuario;
        this.tipoAccionAuditoria = tipoAccionAuditoria;
        this.ipUsuario = "127.0.0.1";
        this.fechaHora = LocalDateTime.now();
    }

    public Auditoria(String entidadAfectada, int idAfectado, String valorAnterior,
                     String valorNuevo, String ipUsuario,
                     TipoAccionAuditoria tipoAccionAuditoria, Usuario usuario) {
        this.entidadAfectada = entidadAfectada;
        this.idAfectado = idAfectado;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.ipUsuario = ipUsuario;
        this.tipoAccionAuditoria = tipoAccionAuditoria;
        this.usuario = usuario;
        this.fechaHora = LocalDateTime.now();
    }

    public int getId() {
        return idAuditoria;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setId(int id) {
        this.idAuditoria = id;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public void setEntidadAfectada(String entidadAfectada) {
        this.entidadAfectada = entidadAfectada;
    }

    public int getIdAfectado() {
        return idAfectado;
    }

    public void setIdAfectado(int idAfectado) {
        this.idAfectado = idAfectado;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getIpUsuario() {
        return ipUsuario;
    }

    public void setIpUsuario(String ipUsuario) {
        this.ipUsuario = ipUsuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoAccionAuditoria getTipoAccionAuditoria() {
        return tipoAccionAuditoria;
    }

    public void setTipoAccionAuditoria(TipoAccionAuditoria tipoAccionAuditoria) {
        this.tipoAccionAuditoria = tipoAccionAuditoria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
