package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad EstadoPago: Catálogo de estados de una transacción de pago.
 * Mapea directamente a la tabla "EstadoPago" en base_datos.sql.
 */
@Entity
@Table(name = "EstadoPago")
public class EstadoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pago")
    private int idEstadoPago;

    /** Nombre del estado de pago (ej. "Pendiente", "Aprobado", "Rechazado"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public EstadoPago() {
    }

    public EstadoPago(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idEstadoPago;
    }

    public int getIdEstadoPago() {
        return idEstadoPago;
    }

    public void setId(int id) {
        this.idEstadoPago = id;
    }

    public void setIdEstadoPago(int idEstadoPago) {
        this.idEstadoPago = idEstadoPago;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
