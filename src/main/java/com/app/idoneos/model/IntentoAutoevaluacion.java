package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad IntentoAutoevaluacion: Rendición de un examen o prueba por parte de un alumno.
 * Mapea directamente a la tabla "IntentoAutoevaluacion" en base_datos.sql.
 */
@Entity
@Table(name = "IntentoAutoevaluacion")
public class IntentoAutoevaluacion {

    /** Identificador único del intento de autoevaluación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Fecha y hora en la que se rindió el intento. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** Calificación o nota obtenida en el intento. */
    @Column(name = "nota", nullable = false)
    private double nota;

    /** Autoevaluación rendida. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoevaluacion_id")
    private Autoevaluacion autoevaluacion;

    /** Alumno que rindió el intento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    public IntentoAutoevaluacion() {}

    public IntentoAutoevaluacion(Autoevaluacion autoevaluacion, Usuario usuario) {
        this.autoevaluacion = autoevaluacion;
        if (usuario != null) {
            this.alumno = usuario.getAlumno();
        }
        this.fecha = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }

    public Autoevaluacion getAutoevaluacion() { return autoevaluacion; }
    public void setAutoevaluacion(Autoevaluacion autoevaluacion) { this.autoevaluacion = autoevaluacion; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
}
