package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad TipoMaterial: Catálogo de clases de material multimedia/lectura.
 * Mapea directamente a la tabla "TipoMaterial" en base_datos.sql.
 */
@Entity
@Table(name = "TipoMaterial")
public class TipoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_material")
    private int idTipoMaterial;

    /** Nombre descriptivo del tipo de material. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public TipoMaterial() {
    }

    public TipoMaterial(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idTipoMaterial;
    }

    public int getIdTipoMaterial() {
        return idTipoMaterial;
    }

    public void setId(int id) {
        this.idTipoMaterial = id;
    }

    public void setIdTipoMaterial(int idTipoMaterial) {
        this.idTipoMaterial = idTipoMaterial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
