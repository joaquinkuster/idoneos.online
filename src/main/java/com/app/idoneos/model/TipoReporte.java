package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de tipos de reporte: Alumnos inscriptos, Tráfico, Ingresos.
 */
@Entity
@Table(name = "tipos_reporte")
@Getter @Setter
@NoArgsConstructor
public class TipoReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public TipoReporte(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
