package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tabla asociativa M:N entre Usuario y Rol.
 * Duplica intencionalmente el rol de Usuario para consultas convenientes
 * sin recorrer las tablas de subtipo. Debe mantenerse sincronizada en la aplicación.
 */
@Entity
@Table(name = "usuario_roles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_rol"}))
@Getter @Setter
@NoArgsConstructor
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
    }
}
