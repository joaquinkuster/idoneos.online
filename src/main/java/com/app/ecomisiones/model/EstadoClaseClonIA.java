package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de estados de ClaseClonIA: Pendiente, Generada, Error.
 */
@Entity
@Table(name = "estados_clase_clon_ia")
@Getter @Setter
@NoArgsConstructor
public class EstadoClaseClonIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public EstadoClaseClonIA(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() { return nombre; }
}
