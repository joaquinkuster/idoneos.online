package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad ClaseClonIA: Clase asincrónica generada por avatar IA a partir de un
 * guión docente.
 * Mapea directamente a la tabla "ClaseClonIA" en base_datos.sql.
 */
@Entity
@Table(name = "ClaseClonIA")
public class ClaseClonIA {

    /** Identificador único de la clase por avatar IA. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Título descriptivo de la clase generada. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /**
     * Guión explicativo enviado al motor/proveedor de generación de video con IA
     * (ej. HeyGen).
     */
    @Column(name = "guion", nullable = false, columnDefinition = "text")
    private String guion;

    /** Fecha y hora en la que se generó la clase. */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    /** Estado de baja lógica del registro. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /**
     * Estado del proceso asincrónico de generación de video (Pendiente, Generada,
     * Error).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_clase_clon_ia_id")
    private EstadoClaseClonIA estadoClaseClonIA;

    /** Material multimedia generado a partir de esta clase (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    /** Unidad temática a la que corresponde la clase. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    /** Docente autor del guión y cuyo avatar fue clonado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    public ClaseClonIA() {
    }

    public ClaseClonIA(String titulo, Unidad unidad, Docente docente, EstadoClaseClonIA estadoClaseClonIA) {
        this.titulo = titulo;
        this.unidad = unidad;
        this.docente = docente;
        this.estadoClaseClonIA = estadoClaseClonIA;
        this.guion = "";
        this.fechaGeneracion = LocalDateTime.now();
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

    public String getGuion() {
        return guion;
    }

    public void setGuion(String guion) {
        this.guion = guion;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
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

    public EstadoClaseClonIA getEstadoClaseClonIA() {
        return estadoClaseClonIA;
    }

    public void setEstadoClaseClonIA(EstadoClaseClonIA estadoClaseClonIA) {
        this.estadoClaseClonIA = estadoClaseClonIA;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }
}
