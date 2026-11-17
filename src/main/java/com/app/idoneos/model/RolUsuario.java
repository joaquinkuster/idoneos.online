package com.app.idoneos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la asignación de un rol a un usuario. */
@Entity
@Table(name = "RolUsuario")
@IdClass(RolUsuario.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class RolUsuario {
    /** Clave primaria compuesta de la asignación de rol. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int usuario;
        private int rol;
    }

    /** Rol de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idRol", nullable = false)
    private Rol rol;

    /** Usuario de este registro. */
    @Id
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
        return "RolUsuario #" + usuario.getIdUsuario() + "-" + rol.getIdRol();
    }
}
