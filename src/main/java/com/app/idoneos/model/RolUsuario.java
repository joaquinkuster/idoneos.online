package com.app.idoneos.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la asignación de un rol a un usuario. */
@Entity
@Table(name = "RolUsuario", uniqueConstraints = { @UniqueConstraint(columnNames = {"idRol", "idUsuario"}) })
@Getter
@Setter
@NoArgsConstructor
public class RolUsuario {

    /** Identificador único de la asignación de rol. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRolUsuario")
    private int idRolUsuario;

    /** Rol de este registro. */
    @ManyToOne
    @JoinColumn(name = "idRol", nullable = false)
    private Rol rol;

    /** Usuario de este registro. */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el rol fue retirado o suspendido para el usuario (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea la asignación de rol con los datos básicos. */
    public RolUsuario(Rol rol, Usuario usuario) {
        this.rol = rol;
        this.usuario = usuario;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la asignación de rol. */
    @Override
    public String toString() {
        return "RolUsuario #" + idRolUsuario;
    }
}
