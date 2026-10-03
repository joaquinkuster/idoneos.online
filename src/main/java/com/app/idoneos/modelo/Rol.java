package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rol que puede tener un usuario en el sistema (administrador, docente o alumno).
 */
@Entity
@Table(name = "Rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    /**
     * Identificador único del rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private int idRol;

    /**
     * Nombre del rol. Debe ser único.
     */
    @Column(name = "nombre", nullable = true, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear un rol con su nombre.
     *
     * @param nombre El nombre del rol (Administrador, Docente o Alumno).
     */
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del rol.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
