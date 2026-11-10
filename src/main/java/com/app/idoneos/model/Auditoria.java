package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una acción realizada por un usuario sobre una entidad del sistema. El registro es inmutable. */
@Entity
@Table(name = "Auditoria")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    /** Identificador único de la auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAuditoria")
    private int idAuditoria;

    /** Tipo auditoria de este registro. */
    @ManyToOne
    @JoinColumn(name = "idTipoAuditoria", nullable = false)
    private TipoAccionAuditoria tipoAuditoria;

    /** Usuario de este registro. */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /** Nombre de la entidad afectada por la acción. */
    @Column(name = "entidadAfectada", nullable = false, length = 50)
    private String entidadAfectada;

    /** Identificador del registro afectado. */
    @Column(name = "idRegistro", nullable = false)
    private int idRegistro;

    /** Dirección IP desde la que el usuario realizó la acción. */
    @Column(name = "ipUsuario", nullable = false, length = 45)
    private String ipUsuario;

    /** Fecha y hora en que se realizó la acción. */
    @Column(name = "fechaHora", nullable = false)
    private LocalDateTime fechaHora;

    /** Detalles auditoria que referencian a este registro. */
    @OneToMany(mappedBy = "auditoria", cascade = CascadeType.ALL)
    private Set<DetalleAuditoria> detallesAuditoria = new HashSet<>();

    /** Crea la auditoría con los datos básicos. */
    public Auditoria(TipoAccionAuditoria tipoAuditoria, Usuario usuario, String entidadAfectada, int idRegistro, String ipUsuario, LocalDateTime fechaHora) {
        this.tipoAuditoria = tipoAuditoria;
        this.usuario = usuario;
        this.entidadAfectada = entidadAfectada;
        this.idRegistro = idRegistro;
        this.ipUsuario = ipUsuario;
        this.fechaHora = fechaHora;
    }

    /** Devuelve una representación en forma de cadena de la auditoría. */
    @Override
    public String toString() {
        return "Auditoria #" + idAuditoria;
    }
}
