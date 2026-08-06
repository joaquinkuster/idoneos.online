package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Material didáctico unificado para todo archivo o contenido de una unidad,
 * sea subido por el docente o generado por IA.
 * Tipos: Grabación, Bibliografía, Presentación, Resumen (catálogo TipoMaterial).
 */
@Entity
@Table(name = "material")
@Getter @Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    /**
     * DDL: fecha_carga timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "publicado", nullable = false)
    private Boolean publicado = true;

    /**
     * Ruta del archivo (grabación, presentación, bibliografía).
     */
    @Column(name = "ruta_archivo", nullable = true, length = 500)
    private String rutaArchivo;

    /**
     * Texto del material (aplica a Resumen). Alternativa a ruta_archivo.
     */
    @Column(name = "contenido", nullable = true, columnDefinition = "TEXT")
    private String contenido;

    /**
     * true si el contenido fue generado por IA; false si lo subió el docente.
     */
    @Column(name = "generado_por_ia", nullable = false)
    private Boolean generadoPorIA = false;

    /**
     * Duración en minutos. Solo aplica a grabaciones.
     */
    @Column(name = "duracion", nullable = true)
    private Integer duracion;

    /**
     * Autor de referencia. Solo aplica a bibliografía.
     */
    @Column(name = "autor", nullable = true, length = 255)
    private String autor;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * DDL: fecha_creacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * DDL: ultima_modificacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "ultima_modificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_material", nullable = false)
    private TipoMaterial tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    /**
     * DDL: id_docente FK — docente que subió o generó el material.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = true)
    private Docente docente;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }

    public Boolean isPublicado() { return publicado; }
    public Boolean getPublicado() { return publicado; }
    public void setPublicado(Boolean publicado) { this.publicado = publicado; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Boolean getGeneradoPorIA() { return generadoPorIA; }
    public void setGeneradoPorIA(Boolean generadoPorIA) { this.generadoPorIA = generadoPorIA; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public Boolean getBaja() { return baja; }
    public void setBaja(Boolean baja) { this.baja = baja; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public TipoMaterial getTipo() { return tipo; }
    public void setTipo(TipoMaterial tipo) { this.tipo = tipo; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Material(TipoMaterial tipo, String titulo, String rutaArchivo, Unidad unidad) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.rutaArchivo = rutaArchivo;
        this.unidad = unidad;
    }
}
