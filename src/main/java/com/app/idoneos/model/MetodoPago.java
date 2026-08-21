package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad MetodoPago: Catálogo de métodos de pago disponibles.
 * Mapea directamente a la tabla "MetodoPago" en base_datos.sql.
 */
@Entity
@Table(name = "MetodoPago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private int idMetodoPago;

    /** Nombre del método de pago (ej. "Tarjeta de crédito", "Transferencia"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public MetodoPago() {
    }

    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idMetodoPago;
    }

    public int getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setId(int id) {
        this.idMetodoPago = id;
    }

    public void setIdMetodoPago(int idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
