package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de auditoría del sistema.
 * Interceptado por Spring AOP (@Aspect) para registrar automáticamente
 * las operaciones relevantes (Crear, Modificar, Eliminar, Consultar).
 */
@Entity
@Table(name = "auditoria")
@Getter @Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_afectado", nullable = false)
    private int idAfectado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_accion", nullable = false)
    private TipoAccionAuditoria tipoAccion;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }

    public int getIdAfectado() { return idAfectado; }
    public void setIdAfectado(int idAfectado) { this.idAfectado = idAfectado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoAccionAuditoria getTipoAccion() { return tipoAccion; }
    public void setTipoAccion(TipoAccionAuditoria tipoAccion) { this.tipoAccion = tipoAccion; }

    public Auditoria(String entidadAfectada, int idAfectado, Usuario usuario, TipoAccionAuditoria tipoAccion) {
        this.entidadAfectada = entidadAfectada;
        this.idAfectado = idAfectado;
        this.usuario = usuario;
        this.tipoAccion = tipoAccion;
    }
}
