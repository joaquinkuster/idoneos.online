package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Unidad: Subdivisión temática de un Programa de estudios.
 * La asociación con un Programa se realiza a través de la entidad Cronograma.
 * Mapea directamente a la tabla "Unidad" en base_datos.sql.
 */
@Entity
@Table(name = "Unidad")
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private int idUnidad;

    /** Título de la unidad. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Breve descripción del alcance temático. */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    /** Contenido detallado de la unidad. */
    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    /** Fecha de creación de la unidad. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación de datos. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica de la unidad. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Entradas de cronograma que vinculan esta unidad a programas. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Cronograma> cronogramas = new ArrayList<>();

    /** Lista de materiales multimedia y lectura asociados. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Material> materiales = new ArrayList<>();

    /** Términos del glosario asociados. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<TerminoGlosario> glosario = new ArrayList<>();

    /** Pools de preguntas asociados. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Pool> pools = new ArrayList<>();

    /** Autoevaluaciones asociadas. */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Autoevaluacion> autoevaluaciones = new ArrayList<>();

    /** Clases en vivo (no FK directa a Unidad en SQL; relación via Cohorte). */
    // ClaseEnVivo referencia Cohorte, no Unidad directamente en el SQL.

    /** Clases clon IA asociadas (no FK directa a Unidad en SQL). */
    // ClaseClon no tiene FK a Unidad en el SQL.

    public Unidad() {
    }

    public Unidad(String titulo, String descripcion, String contenido) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contenido = contenido;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idUnidad;
    }

    public int getIdUnidad() {
        return idUnidad;
    }

    public void setId(int id) {
        this.idUnidad = id;
    }

    public void setIdUnidad(int idUnidad) {
        this.idUnidad = idUnidad;
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
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

    public List<Cronograma> getCronogramas() {
        return cronogramas;
    }

    public void setCronogramas(List<Cronograma> cronogramas) {
        this.cronogramas = cronogramas;
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

    /**
     * Helper para obtener el número de orden de la unidad según el primer cronograma.
     */
    public int getNumeroOrden() {
        if (this.cronogramas != null && !this.cronogramas.isEmpty()) {
            return this.cronogramas.get(0).getNumeroOrden();
        }
        return 1;
    }

    public void setNumeroOrden(int numeroOrden) {
        if (this.cronogramas != null && !this.cronogramas.isEmpty()) {
            this.cronogramas.get(0).setNumeroOrden(numeroOrden);
        }
    }

    /**
     * Helper para obtener el Curso al que pertenece la unidad a través de su Programa/Cronograma.
     */
    public Curso getCurso() {
        if (this.cronogramas != null && !this.cronogramas.isEmpty()) {
            for (Cronograma c : this.cronogramas) {
                if (c.getPrograma() != null && c.getPrograma().getCurso() != null) {
                    return c.getPrograma().getCurso();
                }
            }
        }
        return null;
    }
}

