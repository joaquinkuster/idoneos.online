package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de estados de pago: Pendiente, Acreditado, Rechazado.
 */
@Entity
@Table(name = "estado_pago")
@Getter @Setter
@NoArgsConstructor
public class EstadoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public EstadoPago(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
