package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad EstadoClaseClonIA: Catálogo de estados de una ClaseClon generada por IA.
 * Mapea directamente a la tabla "EstadoClaseClon" en base_datos.sql.
 */
@Entity
@Table(name = "EstadoClaseClon")
public class EstadoClaseClonIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_clase_clon")
    private int idEstadoClaseClon;

    /** Nombre del estado (ej. "Generando", "Disponible", "Error"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public EstadoClaseClonIA() {
    }

    public EstadoClaseClonIA(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idEstadoClaseClon;
    }

    public int getIdEstadoClaseClon() {
        return idEstadoClaseClon;
    }

    public void setId(int id) {
        this.idEstadoClaseClon = id;
    }

    public void setIdEstadoClaseClon(int idEstadoClaseClon) {
        this.idEstadoClaseClon = idEstadoClaseClon;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

