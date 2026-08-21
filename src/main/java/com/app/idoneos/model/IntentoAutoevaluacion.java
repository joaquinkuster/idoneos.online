package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad IntentoAutoevaluacion: Registro del intento de un alumno en una autoevaluación.
 * Mapea directamente a la tabla "IntentoAutoevaluacion" en base_datos.sql.
 */
@Entity
@Table(name = "IntentoAutoevaluacion")
public class IntentoAutoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento_autoevaluacion")
    private int idIntentoAutoevaluacion;

    /** Fecha y hora de entrega del intento (null si aún no se entregó). */
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    /** Nota obtenida en el intento (null si aún no fue calificado). */
    @Column(name = "nota")
    private Float nota;

    /** Estado de baja lógica del intento. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Inscripción del alumno a la que corresponde el intento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;

    /** Autoevaluación intentada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    public IntentoAutoevaluacion() {
    }

    public IntentoAutoevaluacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion) {
        this.inscripcion = inscripcion;
        this.autoevaluacion = autoevaluacion;
    }

    public int getId() {
        return idIntentoAutoevaluacion;
    }

    public int getIdIntentoAutoevaluacion() {
        return idIntentoAutoevaluacion;
    }

    public void setId(int id) {
        this.idIntentoAutoevaluacion = id;
    }

    public void setIdIntentoAutoevaluacion(int idIntentoAutoevaluacion) {
        this.idIntentoAutoevaluacion = idIntentoAutoevaluacion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    /** Alias de compatibilidad con código que use getFecha(). */
    public LocalDateTime getFecha() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fechaEntrega = fecha;
    }

    public Float getNota() {
        return nota;
    }

    public void setNota(Float nota) {
        this.nota = nota;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public Autoevaluacion getAutoevaluacion() {
        return autoevaluacion;
    }

    public void setAutoevaluacion(Autoevaluacion autoevaluacion) {
        this.autoevaluacion = autoevaluacion;
    }
}
