package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Progreso: Seguimiento del estado de avance de un alumno sobre las
 * unidades de un curso.
 * Mapea directamente a la tabla "Progreso" en base_datos.sql.
 */
@Entity
@Table(name = "Progreso")
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso")
    private int idProgreso;

    /** Indica si el alumno completó la unidad. */
    @Column(name = "completada", nullable = false)
    private boolean completada = false;

    /** Fecha y hora en la que el alumno marcó la unidad como completada. */
    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    /** Unidad temática sobre la cual se registra el progreso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    /** Inscripción del alumno a la que corresponde el seguimiento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;

    public Progreso() {
    }

    public Progreso(Inscripcion inscripcion, Unidad unidad, boolean completada) {
        this.inscripcion = inscripcion;
        this.unidad = unidad;
        this.completada = completada;
        if (completada) {
            this.fechaCompletada = LocalDateTime.now();
        }
    }

    public int getId() {
        return idProgreso;
    }

    public int getIdProgreso() {
        return idProgreso;
    }

    public void setId(int id) {
        this.idProgreso = id;
    }

    public void setIdProgreso(int idProgreso) {
        this.idProgreso = idProgreso;
    }

    public boolean isCompletada() {
        return completada;
    }

    public boolean getCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }

    public void setFechaCompletada(LocalDateTime fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }

    public void setFechaCompletado(java.time.LocalDate f) {
        this.fechaCompletada = f != null ? f.atStartOfDay() : null;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }
}
