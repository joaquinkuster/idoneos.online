package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;

/**
 * Representa una unidad académica dentro de un curso de Idóneos Online.
 */
@Entity
@Table(name = "unidad")
@Getter @Setter
@NoArgsConstructor
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", nullable = true, length = 1000)
    private String descripcion;

    @Column(name = "numero_orden", nullable = false)
    private int numeroOrden;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Material> materiales = new ArrayList<>();

    public Unidad(String titulo, String descripcion, int numeroOrden, Curso curso) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroOrden = numeroOrden;
        this.curso = curso;
    }

    @Override
    public String toString() {
        return "Unidad " + numeroOrden + ": " + titulo;
    }
}
