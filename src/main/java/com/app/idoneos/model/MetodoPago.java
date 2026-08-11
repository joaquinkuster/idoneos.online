package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad MétodoPago: Catálogo de medios de pago aceptados (Tarjeta de crédito, Tarjeta de débito, Saldo de cuenta).
 * Mapea directamente a la tabla "MetodoPago" en base_datos.sql.
 */
@Entity
@Table(name = "\"MetodoPago\"")
public class MetodoPago {

    /** Identificador único del método de pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre del método de pago. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public MetodoPago() {}

    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
