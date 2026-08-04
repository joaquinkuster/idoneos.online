package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Descuento aplicable al precio de un curso.
 * cursosRequeridos: cantidad de cursos previos que el alumno debe tener
 * para acceder a este descuento (única condición de negocio existente).
 */
@Entity
@Table(name = "descuento")
@Getter @Setter
@NoArgsConstructor
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "porcentaje", nullable = false)
    private Double porcentaje;

    /**
     * DDL: vigencia_desde timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "vigencia_desde", nullable = false)
    private LocalDateTime vigenciaDesde;

    /**
     * DDL: vigencia_hasta timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    @Column(name = "cantidad_limite", nullable = true)
    private Integer cantidadLimite;

    @Column(name = "cantidad_usada", nullable = false)
    private Integer cantidadUsada = 0;

    @Column(name = "cursos_requeridos", nullable = false)
    private Integer cursosRequeridos = 0;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * DDL: fecha_creacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * DDL: ultima_modificacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "ultima_modificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return !baja
            && !ahora.isBefore(vigenciaDesde)
            && !ahora.isAfter(vigenciaHasta)
            && (cantidadLimite == null || cantidadUsada < cantidadLimite);
    }
}
