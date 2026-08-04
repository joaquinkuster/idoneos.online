package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de roles: Alumno, Docente, Administrador.
 * Se mantiene en paralelo con RolUsuario (enum en Usuario) para facilitar
 * consultas sin recorrer las tres tablas de subtipo.
 */
@Entity
@Table(name = "rol")
@Getter @Setter
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
