package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Rol: Catálogo de roles de seguridad del sistema (Alumno, Docente,
 * Administrador).
 * Mapea directamente a la tabla "Rol" en base_datos.sql.
 */
@Entity
@Table(name = "Rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private int idRol;

    /** Nombre del rol de usuario (ej. "Alumno", "Docente", "Administrador"). */
    @Column(name = "nombre", length = 50)
    private String nombre;

    public Rol() {
    }

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idRol;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setId(int id) {
        this.idRol = id;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
