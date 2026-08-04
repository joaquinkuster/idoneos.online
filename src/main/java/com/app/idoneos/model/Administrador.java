package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Subtipo de Usuario para administradores.
 * Sin atributos propios: su existencia en la tabla garantiza integridad referencial.
 */
@Entity
@Table(name = "administradores")
@Getter @Setter
@NoArgsConstructor
public class Administrador {

    @Id
    @Column(name = "id_usuario")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    public Administrador(Usuario usuario) {
        this.usuario = usuario;
        this.id = usuario.getId();
    }

    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }
}
