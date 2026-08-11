package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Rol: Catálogo de roles de seguridad del sistema (Alumno, Docente, Administrador).
 * Mapea directamente a la tabla "Rol" en base_datos.sql.
 */
@Entity
@Table(name = "\"Rol\"")
public class Rol {

    /** Identificador único del rol. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre del rol de usuario (ej. "Alumno", "Docente", "Administrador"). */
    @Column(name = "nombre", length = 50)
    private String nombre;

    public Rol() {}

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
