package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa un curso académico en la plataforma Idóneos Online.
 * precio = 0 indica gratuito (no existe atributo esGratuito separado).
 * La relación con docentes se maneja vía DocenteCurso (tabla asociativa M:N).
 */
@Entity
@Table(name = "curso")
@Getter @Setter
@NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String descripcion;

    @Column(name = "precio", nullable = false)
    private float precio = 0f;

    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    @Column(name = "publicado", nullable = false)
    private Boolean publicado = true;

    /**
     * DDL: timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_inicio_inscripcion", nullable = true)
    private LocalDateTime fechaInicioInscripcion;

    /**
     * DDL: timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_fin_inscripcion", nullable = true)
    private LocalDateTime fechaFinInscripcion;

    /**
     * Meses de acceso desde la fecha de inscripción. Se usa para calcular
     * Inscripcion.fechaVencimientoAcceso.
     */
    @Column(name = "meses_acceso", nullable = true)
    private Integer mesesAcceso;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * DDL: timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * DDL: ultima_modificacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "ultima_modificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroOrden ASC")
    private List<Unidad> unidades = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocenteCurso> docentes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "modalidad_curso",
        joinColumns = @JoinColumn(name = "id_curso"),
        inverseJoinColumns = @JoinColumn(name = "id_modalidad")
    )
    private Set<Modalidad> modalidades = new HashSet<>();

    public Curso(String nombre, String descripcion, float precio, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
    }

    public boolean esGratuito() {
        return precio == 0f;
    }

    /**
     * Retorna true si más del 50% de las unidades del curso tienen materiales generados por Clon IA.
     */
    public boolean esCursoConClonIA() {
        if (unidades == null || unidades.isEmpty()) return false;
        long unidadesClon = unidades.stream()
                .filter(u -> u.getMateriales().stream().anyMatch(m -> !m.getBaja() && Boolean.TRUE.equals(m.getGeneradoPorIA())))
                .count();
        return (unidadesClon * 100.0 / unidades.size()) >= 50.0;
    }

    /**
     * Devuelve el docente titular (es_supervisor = false).
     */
    public Docente getDocenteTitular() {
        return docentes.stream()
                .filter(dc -> !dc.isEsSupervisor())
                .map(DocenteCurso::getDocente)
                .findFirst()
                .orElse(null);
    }

    /**
     * Devuelve el docente supervisor (es_supervisor = true), si existe.
     */
    public Docente getDocenteSupervisor() {
        return docentes.stream()
                .filter(DocenteCurso::isEsSupervisor)
                .map(DocenteCurso::getDocente)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
