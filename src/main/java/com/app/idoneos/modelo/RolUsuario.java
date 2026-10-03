package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Asignación de un rol a un usuario. Indica qué roles tiene cada usuario y si siguen vigentes.
 */
@Entity
@Table(name = "RolUsuario", uniqueConstraints = { @UniqueConstraint(columnNames = {"idRol", "idUsuario"}) })
@Getter
@Setter
@NoArgsConstructor
public class RolUsuario {

    /**
     * Identificador único de la asignación de rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRolUsuario")
    private int idRolUsuario;

    /**
     * Relación con Rol.
     */
    @ManyToOne
    @JoinColumn(name = "idRol", nullable = false)
    private Rol rol;

    /**
     * Relación con Usuario.
     */
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el rol fue retirado o suspendido para el usuario (baja lógica). El valor predeterminado es "false", indicando que el rol está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Constructor para crear la asignación de rol con los datos básicos.
     *
     * @param rol relación con Rol.
     * @param usuario relación con Usuario.
     */
    public RolUsuario(Rol rol, Usuario usuario) {
        this.rol = rol;
        this.usuario = usuario;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena de la asignación de rol.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "RolUsuario #" + idRolUsuario;
    }
}
