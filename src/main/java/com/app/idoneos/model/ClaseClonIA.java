package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad ClaseClonIA: Clase generada mediante tecnología de Clon IA (avatar + voz sintetizada).
 * Mapea directamente a la tabla "ClaseClon" en base_datos.sql.
 * Nota: el nombre de clase se mantiene como ClaseClonIA por compatibilidad con servicios existentes.
 */
@Entity
@Table(name = "ClaseClon")
public class ClaseClonIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clase_clon")
    private int idClaseClon;

    /** Título de la clase generada. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Guión textual que el avatar IA reproduce. */
    @Column(name = "guion", nullable = false, columnDefinition = "text")
    private String guion;

    /** Fecha y hora de generación de la clase. */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    /** Indica si la clase está oculta para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private boolean oculto = false;

    /** Estado de baja lógica de la clase. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Docente autor de la clase con Clon IA. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /** Estado de generación/disponibilidad de la clase. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_clase_clon", nullable = false)
    private EstadoClaseClonIA estadoClaseClon;

    /** Material multimedia asociado (puede ser nulo si aún no se generó). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    private Material material;

    /**
     * Unidad asociada — campo de compatibilidad para servicios existentes.
     * No persiste en BD (ClaseClon no tiene FK a Unidad en el SQL).
     */
    @Transient
    private Unidad unidad;

    public ClaseClonIA() {
    }

    public ClaseClonIA(String titulo, String guion, Docente docente, EstadoClaseClonIA estadoClaseClon) {
        this.titulo = titulo;
        this.guion = guion;
        this.docente = docente;
        this.estadoClaseClon = estadoClaseClon;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public int getId() {
        return idClaseClon;
    }

    public int getIdClaseClon() {
        return idClaseClon;
    }

    public void setId(int id) {
        this.idClaseClon = id;
    }

    public void setIdClaseClon(int idClaseClon) {
        this.idClaseClon = idClaseClon;
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

    public boolean isOculto() {
        return oculto;
    }

    public boolean getOculto() {
        return oculto;
    }

    public void setOculto(boolean oculto) {
        this.oculto = oculto;
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

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public EstadoClaseClonIA getEstadoClaseClon() {
        return estadoClaseClon;
    }

    public void setEstadoClaseClon(EstadoClaseClonIA estadoClaseClon) {
        this.estadoClaseClon = estadoClaseClon;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    /** Compatibilidad con ClaseClonIAServiceImpl: campo @Transient. */
    public Unidad getUnidad() {
        return unidad;
    }

    /** Compatibilidad con ClaseClonIAServiceImpl: campo @Transient. */
    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }
}
