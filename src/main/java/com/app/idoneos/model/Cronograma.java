package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad Cronograma: Vinculación ordenada entre un Programa y sus Unidades temáticas.
 * Define el orden de cursado y la duración en semanas de cada unidad dentro del programa.
 * Mapea directamente a la tabla "Cronograma" en base_datos.sql.
 */
@Entity
@Table(name = "Cronograma")
public class Cronograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Posición u orden de presentación de la unidad dentro del programa. */
    @Column(name = "numero_orden", nullable = false)
    private int numeroOrden;

    /** Duración estimada de la unidad en semanas. */
    @Column(name = "semanas_duracion", nullable = false)
    private int semanasDuracion;

    /** Programa al que pertenece esta entrada del cronograma. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_programa", nullable = false)
    private Programa programa;

    /** Unidad temática vinculada a esta entrada del cronograma. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public Cronograma() {
    }

    public Cronograma(int numeroOrden, int semanasDuracion, Programa programa, Unidad unidad) {
        this.numeroOrden = numeroOrden;
        this.semanasDuracion = semanasDuracion;
        this.programa = programa;
        this.unidad = unidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(int numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public int getSemanasDuracion() {
        return semanasDuracion;
    }

    public void setSemanasDuracion(int semanasDuracion) {
        this.semanasDuracion = semanasDuracion;
    }

    public Programa getPrograma() {
        return programa;
    }

    public void setPrograma(Programa programa) {
        this.programa = programa;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }
}

