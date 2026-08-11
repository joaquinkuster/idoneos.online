package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Categoría: Clasificación temática de los cursos (ej. Finanzas
 * Personales, Economía).
 * Mapea directamente a la tabla "Categoria" en base_datos.sql.
 */
@Entity
@Table(name = "Categoria")
public class Categoria {

    /** Identificador único de la categoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre descriptivo de la categoría. */
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /** Breve resumen del ámbito cubierto por la categoría. */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última actualización del registro. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Marca de baja lógica. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    public Categoria() {
    }

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public void setUltimaModificacion(LocalDateTime ultimaModificacion) {
        this.ultimaModificacion = ultimaModificacion;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public boolean esInactivo() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }
}
