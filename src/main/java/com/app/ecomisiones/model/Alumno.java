package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Subtipo de Usuario para alumnos.
 * Sin atributos propios: su existencia en la tabla garantiza integridad referencial.
 * Una FK a Alumno asegura a nivel de BD que ese usuario tiene rol de alumno.
 */
@Entity
@Table(name = "alumnos")
@Getter @Setter
@NoArgsConstructor
public class Alumno {

    @Id
    @Column(name = "id_usuario")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    public Alumno(Usuario usuario) {
        this.usuario = usuario;
        this.id = usuario.getId();
    }

    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }
}
