package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un rol que puede tener un usuario. Puede ser administrador, docente o alumno. */
@Entity
@Table(name = "Rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    /** Identificador único del rol. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private int idRol;

    /** Nombre del rol. */
    @Column(name = "nombre", nullable = true, length = 50, unique = true)
    private String nombre;

    /** Crea un rol con su nombre. */
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del rol. */
    @Override
    public String toString() {
        return nombre;
    }
}
