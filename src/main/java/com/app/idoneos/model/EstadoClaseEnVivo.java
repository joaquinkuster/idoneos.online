package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad EstadoClaseEnVivo: Catálogo de estados de transmisión en vivo
 * (Programada, En vivo, Finalizada).
 * Mapea directamente a la tabla "EstadoClaseEnVIvo" en base_datos.sql.
 */
@Entity
@Table(name = "EstadoClaseEnVIvo")
public class EstadoClaseEnVivo {

    /** Identificador único del estado de transmisión en vivo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre del estado (ej. "Programada", "En vivo", "Finalizada"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public EstadoClaseEnVivo() {
    }

    public EstadoClaseEnVivo(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
