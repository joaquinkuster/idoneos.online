package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad UsuarioRol: Tabla asociativa M a N entre Usuario y Rol ("Usuario
 * Rol").
 * Mapea directamente a la tabla "Usuario Rol" en base_datos.sql.
 */
@Entity
@Table(name = "UsuarioRol")
public class UsuarioRol {

    /** Identificador único de la vinculación usuario-rol. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Usuario asociado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Rol asignado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    public UsuarioRol() {
    }

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
