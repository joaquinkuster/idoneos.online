package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro inmutable de una acción realizada por un usuario sobre una entidad del sistema (alta, modificación o baja).
 */
@Entity
@Table(name = "Auditoria")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    /**
     * Identificador único de la auditoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAuditoria")
    private int idAuditoria;

    /**
     * Relación con TipoAccionAuditoria.
     */
    @ManyToOne
    @JoinColumn(name = "idTipoAuditoria", nullable = false)
    private TipoAccionAuditoria tipoAuditoria;

    /**
     * Relación con Usuario.
     */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /**
     * Nombre de la entidad afectada por la acción.
     */
    @Column(name = "entidadAfectada", nullable = false, length = 50)
    private String entidadAfectada;

    /**
     * Identificador del registro afectado.
     */
    @Column(name = "idRegistro", nullable = false)
    private int idRegistro;

    /**
     * Dirección IP desde la que el usuario realizó la acción.
     */
    @Column(name = "ipUsuario", nullable = false, length = 45)
    private String ipUsuario;

    /**
     * Fecha y hora en que se realizó la acción.
     */
    @Column(name = "fechaHora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Conjunto de detallesAuditoria asociados.
     */
    @OneToMany(mappedBy = "auditoria", cascade = CascadeType.ALL)
    private Set<DetalleAuditoria> detallesAuditoria = new HashSet<>();

    /**
     * Constructor para crear la auditoría con los datos básicos.
     *
     * @param tipoAuditoria relación con TipoAccionAuditoria.
     * @param usuario relación con Usuario.
     * @param entidadAfectada nombre de la entidad afectada por la acción.
     * @param idRegistro identificador del registro afectado.
     * @param ipUsuario dirección IP desde la que el usuario realizó la acción.
     * @param fechaHora fecha y hora en que se realizó la acción.
     */
    public Auditoria(TipoAccionAuditoria tipoAuditoria, Usuario usuario, String entidadAfectada, int idRegistro, String ipUsuario, LocalDateTime fechaHora) {
        this.tipoAuditoria = tipoAuditoria;
        this.usuario = usuario;
        this.entidadAfectada = entidadAfectada;
        this.idRegistro = idRegistro;
        this.ipUsuario = ipUsuario;
        this.fechaHora = fechaHora;
    }

    /**
     * Devuelve una representación en forma de cadena de la auditoría.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Auditoria #" + idAuditoria;
    }
}
