package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad TipoReporte: Catálogo de tipos de reportes de administración (Alumnos
 * inscriptos, Ingresos).
 * Mapea directamente a la tabla "TipoReporte" en base_datos.sql.
 */
@Entity
@Table(name = "TipoReporte")
public class TipoReporte {

    /** Identificador único del tipo de reporte. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre del tipo de reporte. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public TipoReporte() {
    }

    public TipoReporte(String nombre) {
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
