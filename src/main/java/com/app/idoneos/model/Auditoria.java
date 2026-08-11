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
}
