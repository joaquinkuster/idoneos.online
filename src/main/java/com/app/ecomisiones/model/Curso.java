package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa un curso académico en la plataforma Idóneos Online.
 */
@Entity
@Table(name = "cursos")
@Getter @Setter
@NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "precio", nullable = false)
    private float precio = 0f;

    @Column(name = "imagen_portada", nullable = true, length = 500)
    private String imagenPortada;

    @Column(name = "publicado", nullable = false)
    private Boolean publicado = true;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_docente_titular", nullable = false)
    private Usuario docenteTitular;

    @ManyToOne
    @JoinColumn(name = "id_docente_supervisor", nullable = true)
    private Usuario docenteSupervisor;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroOrden ASC")
    private List<Unidad> unidades = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    public Curso(String nombre, String descripcion, float precio, Categoria categoria, Usuario docenteTitular) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.docenteTitular = docenteTitular;
    }

    public boolean esGratuito() {
        return precio == 0f;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
