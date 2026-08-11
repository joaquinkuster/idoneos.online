package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Modalidad: Catálogo de modalidades de cursada (En vivo, Grabada, Clon IA).
 * Mapea directamente a la tabla "Modalidad" en base_datos.sql.
 */
@Entity
@Table(name = "\"Modalidad\"")
public class Modalidad {

    /** Identificador único de la modalidad. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre de la modalidad de dictado. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public Modalidad() {}

    public Modalidad(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
