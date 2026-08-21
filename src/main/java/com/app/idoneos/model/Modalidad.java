package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad Modalidad: Catálogo de modalidades de cursado (ej. Sincrónico, Asincrónico).
 * Mapea directamente a la tabla "Modalidad" en base_datos.sql.
 */
@Entity
@Table(name = "Modalidad")
public class Modalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modalidad")
    private int idModalidad;

    /** Nombre de la modalidad. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public Modalidad() {
    }

    public Modalidad(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idModalidad;
    }

    public int getIdModalidad() {
        return idModalidad;
    }

    public void setId(int id) {
        this.idModalidad = id;
    }

    public void setIdModalidad(int idModalidad) {
        this.idModalidad = idModalidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

