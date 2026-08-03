package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Descuento aplicable al precio de un curso.
 * cursoRequeridos: cantidad de cursos previos que el alumno debe tener
 * para acceder a este descuento (única condición de negocio existente).
 */
@Entity
@Table(name = "descuentos")
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

    @Column(name = "vigencia_desde", nullable = false)
    private LocalDate vigenciaDesde;

    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDate vigenciaHasta;

    @Column(name = "cantidad_limite", nullable = true)
    private Integer cantidadLimite;

    @Column(name = "cantidad_usada", nullable = false)
    private Integer cantidadUsada = 0;

    @Column(name = "cursos_requeridos", nullable = false)
    private Integer cursosRequeridos = 0;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !baja
            && !hoy.isBefore(vigenciaDesde)
            && !hoy.isAfter(vigenciaHasta)
            && (cantidadLimite == null || cantidadUsada < cantidadLimite);
    }
}
