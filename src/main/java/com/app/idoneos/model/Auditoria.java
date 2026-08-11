package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Auditoría: Registro trazable AOP de operaciones sobre entidades del sistema.
 * Mapea directamente a la tabla "Auditoria" en base_datos.sql.
 */
@Entity
@Table(name = "\"Auditoria\"")
public class Auditoria {

    /** Identificador único del registro de auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre de la entidad/tabla afectada por la acción (ej. "Curso", "Usuario"). */
    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    /** Identificador primario del registro puntual afectado. */
    @Column(name = "id_afectado", nullable = false)
    private int idAfectado;

    /** Representación del estado del registro previo a la modificación (JSON/texto). */
    @Column(name = "valor_anterior", columnDefinition = "text")
    private String valorAnterior;

    /** Representación del estado del registro posterior a la modificación (JSON/texto). */
    @Column(name = "valor_nuevo", columnDefinition = "text")
    private String valorNuevo;

    /** Dirección IP desde donde el usuario ejecutó la acción. */
    @Column(name = "ip_usuario", nullable = false, length = 45)
    private String ipUsuario;

    /** Fecha y hora exacta de la acción auditada. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    /** Usuario que realizó la acción. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Tipo de acción de auditoría ejecutada (Crear, Modificar, Eliminar, Consultar). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_accion_auditoria_id")
    private TipoAccionAuditoria tipoAccionAuditoria;

    public Auditoria() {}

    public Auditoria(String entidadAfectada, int idAfectado, Usuario usuario, TipoAccionAuditoria tipoAccionAuditoria) {
        this.entidadAfectada = entidadAfectada;
        this.idAfectado = idAfectado;
        this.usuario = usuario;
        this.tipoAccionAuditoria = tipoAccionAuditoria;
        this.fechaHora = LocalDateTime.now();
        this.ipUsuario = "127.0.0.1";
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
