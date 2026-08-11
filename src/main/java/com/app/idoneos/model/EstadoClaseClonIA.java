package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad EstadoClaseClonIA: Catálogo de estados de generación asincrónica de video con IA (Pendiente, Generada, Error).
 * Mapea directamente a la tabla "EstadoClaseClonIA" en base_datos.sql.
 */
@Entity
@Table(name = "\"EstadoClaseClonIA\"")
public class EstadoClaseClonIA {

    /** Identificador único del estado de clase clon IA. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre del estado (ej. "Pendiente", "Generada", "Error"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public EstadoClaseClonIA() {}

    public EstadoClaseClonIA(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
