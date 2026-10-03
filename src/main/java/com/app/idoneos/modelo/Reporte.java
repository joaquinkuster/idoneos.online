package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reporte generado por un administrador sobre un curso.
 */
@Entity
@Table(name = "Reporte")
@Getter
@Setter
@NoArgsConstructor
public class Reporte {

    /**
     * Identificador único del reporte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReporte")
    private int idReporte;

    /**
     * Relación con Administrador.
     */
    @ManyToOne
    @JoinColumn(name = "idAdministrador", nullable = false)
    private Administrador administrador;

    /**
     * Relación con TipoReporte.
     */
    @ManyToOne
    @JoinColumn(name = "idTipoReporte", nullable = false)
    private TipoReporte tipoReporte;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Fecha y hora de generación del reporte.
     */
    @Column(name = "fechaGeneracion", nullable = false)
    private LocalDateTime fechaGeneracion;

    /**
     * Constructor para crear el reporte con los datos básicos.
     *
     * @param administrador relación con Administrador.
     * @param tipoReporte relación con TipoReporte.
     * @param curso relación con Curso.
     * @param fechaGeneracion fecha y hora de generación del reporte.
     */
    public Reporte(Administrador administrador, TipoReporte tipoReporte, Curso curso, LocalDateTime fechaGeneracion) {
        this.administrador = administrador;
        this.tipoReporte = tipoReporte;
        this.curso = curso;
        this.fechaGeneracion = fechaGeneracion;
    }

    /**
     * Devuelve una representación en forma de cadena del reporte.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Reporte #" + idReporte;
    }
}
