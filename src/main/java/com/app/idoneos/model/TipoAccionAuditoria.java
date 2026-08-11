package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad TipoAcciónAuditoria: Catálogo de acciones de auditoría (Crear,
 * Modificar, Eliminar, Consultar).
 * Mapea directamente a la tabla "TipoAccionAuditoria" en base_datos.sql.
 */
@Entity
@Table(name = "TipoAccionAuditoria")
public class TipoAccionAuditoria {

    /** Identificador único del tipo de acción de auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * Nombre del tipo de acción (ej. "Crear", "Modificar", "Eliminar",
     * "Consultar").
     */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public TipoAccionAuditoria() {
    }

    public TipoAccionAuditoria(String nombre) {
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
