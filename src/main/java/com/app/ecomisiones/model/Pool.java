package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Banco de preguntas de una unidad. Relación 1:0..1 con Unidad.
 */
@Entity
@Table(name = "pools")
@Getter @Setter
@NoArgsConstructor
public class Pool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false, unique = true)
    private Unidad unidad;

    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    public Pool(String nombre, Unidad unidad) {
        this.nombre = nombre;
        this.unidad = unidad;
    }
}
