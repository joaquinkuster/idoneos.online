package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;

/**
 * Entidad EstadoClaseEnVivo: Catálogo de estados de una clase transmitida en vivo.
 * Mapea directamente a la tabla "EstadoClaseEnVIvo" en base_datos.sql.
 */
@Entity
@Table(name = "EstadoClaseEnVIvo")
public class EstadoClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_clase_en_vivo")
    private int idEstadoClaseEnVivo;

    /** Nombre del estado (ej. "Programada", "En Vivo", "Finalizada"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public EstadoClaseEnVivo() {
    }

    public EstadoClaseEnVivo(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idEstadoClaseEnVivo;
    }

    public int getIdEstadoClaseEnVivo() {
        return idEstadoClaseEnVivo;
    }

    public void setId(int id) {
        this.idEstadoClaseEnVivo = id;
    }

    public void setIdEstadoClaseEnVivo(int idEstadoClaseEnVivo) {
        this.idEstadoClaseEnVivo = idEstadoClaseEnVivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

