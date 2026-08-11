package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una versión del plan de estudios de un Curso.
 * Organiza las unidades temáticas y los dictados de clases.
 */
@Entity
@Table(name = "Programa")
public class Programa {

    /** Identificador único del programa. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre de la versión del programa (ej. "Edición 2026"). */
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /** Descripción sintética del plan de estudios. */
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /** Meses de acceso habilitados al inscribirse a un dictado de este programa. */
    @Column(name = "meses_acceso", nullable = false)
    private int mesesAcceso;

    /** Fecha y hora de creación del programa. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Marca de baja lógica del programa. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Curso al que pertenece esta versión de programa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    /** Lista de dictados de clases organizados bajo este programa. */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private List<Dictado> dictados = new ArrayList<>();

    /** Lista de unidades académicas que componen el contenido de este programa. */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private List<Unidad> unidades = new ArrayList<>();

    public Programa() {
    }

    public Programa(String nombre, String descripcion, int mesesAcceso, Curso curso) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.mesesAcceso = mesesAcceso;
        this.curso = curso;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getMesesAcceso() {
        return mesesAcceso;
    }

    public void setMesesAcceso(int mesesAcceso) {
        this.mesesAcceso = mesesAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public void setUltimaModificacion(LocalDateTime ultimaModificacion) {
        this.ultimaModificacion = ultimaModificacion;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<Dictado> getDictados() {
        return dictados;
    }

    public void setDictados(List<Dictado> dictados) {
        this.dictados = dictados;
    }

    public List<Unidad> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<Unidad> unidades) {
        this.unidades = unidades;
    }
}
