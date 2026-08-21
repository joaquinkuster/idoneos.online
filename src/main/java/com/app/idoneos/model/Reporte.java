package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Reporte: Informe generado por un Administrador sobre un Curso.
 * Mapea directamente a la tabla "Reporte" en base_datos.sql.
 */
@Entity
@Table(name = "Reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private int idReporte;

    /** Fecha y hora en la que se generó el reporte. */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    /** Administrador que solicitó la generación del reporte. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador", nullable = false)
    private Administrador administrador;

    /** Tipo de reporte generado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_reporte", nullable = false)
    private TipoReporte tipoReporte;

    /** Curso sobre el cual se genera el reporte. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    public Reporte() {
    }

    public Reporte(TipoReporte tipoReporte, Administrador administrador) {
        this.tipoReporte = tipoReporte;
        this.administrador = administrador;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public Reporte(TipoReporte tipoReporte, Administrador administrador, Curso curso) {
        this.tipoReporte = tipoReporte;
        this.administrador = administrador;
        this.curso = curso;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public int getId() {
        return idReporte;
    }

    public int getIdReporte() {
        return idReporte;
    }

    public void setId(int id) {
        this.idReporte = id;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public TipoReporte getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(TipoReporte tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}

