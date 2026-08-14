package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Unidad: Subdivisión de un Programa de estudios. Nodo central del que
 * depende el contenido.
 * Mapea directamente a la tabla "Unidad" en base_datos.sql.
 */
@Entity
@Table(name = "Unidad")
public class Unidad {

    /** Identificador único de la unidad temática. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Título de la unidad. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Breve descripción del alcance temático. */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    /** Posición u orden de presentación secuencial dentro del curso. */
    @Column(name = "numero_orden", nullable = false)
    private int numeroOrden;

    /** Fecha de creación de la unidad. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación de datos. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica de la unidad. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Programa al que pertenece esta unidad. */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    /** Lista de materiales multimedia y lectura asociados. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Material> materiales = new ArrayList<>();

    /** Términos del glosario asociados. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<TerminoGlosario> glosario = new ArrayList<>();

    /** Pools de preguntas asociados. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Pool> pools = new ArrayList<>();

    /** Autoevaluaciones rrendibles asociadas. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Autoevaluacion> autoevaluaciones = new ArrayList<>();

    /** Clases en vivo asociadas. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();

    /** Clases clon IA asociadas. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    public Unidad() {
    }

    public Unidad(String titulo, String descripcion, int numeroOrden, Programa programa) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroOrden = numeroOrden;
        this.programa = programa;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Unidad(String titulo, String descripcion, int numeroOrden, Curso curso) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroOrden = numeroOrden;
        if (curso != null) {
            this.programa = new Programa(curso.getNombre(), "Programa por defecto", 12, curso);
        }
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(int numeroOrden) {
        this.numeroOrden = numeroOrden;
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

    public Programa getPrograma() {
        return programa;
    }

    public void setPrograma(Programa programa) {
        this.programa = programa;
    }

    public Curso getCurso() {
        return programa != null ? programa.getCurso() : null;
    }

    public List<Material> getMateriales() {
        return materiales;
    }

    public void setMateriales(List<Material> materiales) {
        this.materiales = materiales;
    }

    public List<TerminoGlosario> getGlosario() {
        return glosario;
    }

    public void setGlosario(List<TerminoGlosario> glosario) {
        this.glosario = glosario;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public void setPools(List<Pool> pools) {
        this.pools = pools;
    }

    public List<Autoevaluacion> getAutoevaluaciones() {
        return autoevaluaciones;
    }

    public void setAutoevaluaciones(List<Autoevaluacion> autoevaluaciones) {
        this.autoevaluaciones = autoevaluaciones;
    }

    public List<ClaseEnVivo> getClasesEnVivo() {
        return clasesEnVivo;
    }

    public void setClasesEnVivo(List<ClaseEnVivo> clasesEnVivo) {
        this.clasesEnVivo = clasesEnVivo;
    }

    public List<ClaseClonIA> getClasesClonIA() {
        return clasesClonIA;
    }

    public void setClasesClonIA(List<ClaseClonIA> clasesClonIA) {
        this.clasesClonIA = clasesClonIA;
    }
}
