package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de métodos de pago: Tarjeta de crédito, Tarjeta de débito.
 * Solo tarjeta (crédito/débito) per alcance del PMV.
 */
@Entity
@Table(name = "metodos_pago")
@Getter @Setter
@NoArgsConstructor
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public MetodoPago(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
