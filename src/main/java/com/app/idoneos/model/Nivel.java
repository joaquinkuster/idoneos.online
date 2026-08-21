package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad Nivel: Catálogo de niveles de dificultad de un curso
 * (Básico, Intermedio, Avanzado).
 * Mapea directamente a la tabla "Nivel" en base_datos.sql.
 */
@Entity
@Table(name = "Nivel")
public class Nivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel")
    private int idNivel;

    /** Nombre del nivel (ej. "Básico", "Intermedio", "Avanzado"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public Nivel() {
    }

    public Nivel(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idNivel;
    }

    public int getIdNivel() {
        return idNivel;
    }

    public void setId(int id) {
        this.idNivel = id;
    }

    public void setIdNivel(int idNivel) {
        this.idNivel = idNivel;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

