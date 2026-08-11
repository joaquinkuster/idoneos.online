package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Reporte: Informe consolidado generado por un Administrador (ej. Alumnos inscriptos, Ingresos).
 * Mapea directamente a la tabla "Reporte" en base_datos.sql.
 */
@Entity
@Table(name = "\"Reporte\"")
public class Reporte {

    /** Identificador único del reporte. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Fecha y hora en la que se solicitó/generó el reporte. */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    /** Tipo de reporte generado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_reporte_id")
    private TipoReporte tipoReporte;

    /** Administrador que solicitó la generación del reporte. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Administrador administrador;

    public Reporte() {}

    public Reporte(TipoReporte tipoReporte, Administrador administrador) {
        this.tipoReporte = tipoReporte;
        this.administrador = administrador;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public TipoReporte getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(TipoReporte tipoReporte) { this.tipoReporte = tipoReporte; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador administrador) { this.administrador = administrador; }
}
