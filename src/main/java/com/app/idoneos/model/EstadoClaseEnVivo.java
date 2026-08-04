package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de estados de ClaseEnVivo: Programada, En vivo, Finalizada.
 */
@Entity
@Table(name = "estados_clase_en_vivo")
@Getter @Setter
@NoArgsConstructor
public class EstadoClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public EstadoClaseEnVivo(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() { return nombre; }
}
