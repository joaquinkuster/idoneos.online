package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_afectado", nullable = false)
    private int idAfectado;

    @Column(name = "valor_anterior", columnDefinition = "text")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "text")
    private String valorNuevo;

    @Column(name = "ip_usuario", nullable = false, length = 45)
    private String ipUsuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_accion_auditoria_id", nullable = false)
    private TipoAccionAuditoria tipoAccionAuditoria;

    public Auditoria(String entidadAfectada, int idAfectado, Usuario usuario, TipoAccionAuditoria tipoAccionAuditoria) {
        this.entidadAfectada = entidadAfectada;
        this.idAfectado = idAfectado;
        this.usuario = usuario;
        this.tipoAccionAuditoria = tipoAccionAuditoria;
        this.ipUsuario = "127.0.0.1";
        this.fechaHora = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }

    public int getIdAfectado() { return idAfectado; }
    public void setIdAfectado(int idAfectado) { this.idAfectado = idAfectado; }

    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }

    public String getValorNuevo() { return valorNuevo; }
    public void setValorNuevo(String valorNuevo) { this.valorNuevo = valorNuevo; }

    public String getIpUsuario() { return ipUsuario; }
    public void setIpUsuario(String ipUsuario) { this.ipUsuario = ipUsuario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoAccionAuditoria getTipoAccionAuditoria() { return tipoAccionAuditoria; }
    public void setTipoAccionAuditoria(TipoAccionAuditoria tipoAccionAuditoria) { this.tipoAccionAuditoria = tipoAccionAuditoria; }
}
