package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad TipoMaterial: Catálogo de clases de material multimedia/lectura
 * (Grabación, Bibliografía, Presentación, Resumen).
 * Mapea directamente a la tabla "TipoMaterial" en base_datos.sql.
 */
@Entity
@Table(name = "TipoMaterial")
public class TipoMaterial {

    /** Identificador único del tipo de material. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre descriptivo del tipo de material. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public TipoMaterial() {
    }

    public TipoMaterial(String nombre) {
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
