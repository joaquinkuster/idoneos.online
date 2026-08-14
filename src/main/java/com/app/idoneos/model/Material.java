package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Material: Archivo o contenido de lectura genérico de una Unidad
 * (Grabación, Bibliografía, Presentación, Resumen).
 * Mapea directamente a la tabla "Material" en base_datos.sql.
 */
@Entity
@Table(name = "Material")
public class Material {

    /** Identificador único del material. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Título descriptivo del material. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Ruta o ubicación del archivo asociado (ej. "videos/u1_clase.mp4"). */
    @Column(name = "ruta_archivo", length = 150)
    private String rutaArchivo;

    /**
     * Contenido textual si el material se presenta en texto directo (ej. Resumen).
     */
    @Column(name = "contenido", length = 500)
    private String contenido;

    /** Duración en minutos (aplica a grabaciones de video). */
    @Column(name = "duracion")
    private Integer duracion;

    /** Nombre del autor de referencia (aplica a bibliografía). */
    @Column(name = "autor", length = 50)
    private String autor;

    /** Indicador de si el contenido fue generado por Inteligencia Artificial. */
    @Column(name = "generado_por_ia", nullable = false)
    private boolean generadoPorIa = false;

    /** Fecha y hora en la que se cargó o generó el material. No persiste en BD (campo transient). */
    @Transient
    private LocalDateTime fechaCarga = LocalDateTime.now();

    /** Indicador de visibilidad pública del material para alumnos. */
    @Column(name = "publicado", nullable = false)
    private boolean publicado = true;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación de datos. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del material. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática a la que pertenece el material. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    /**
     * Tipo o categoría de material (Grabación, Bibliografía, Presentación,
     * Resumen).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_material_id")
    private TipoMaterial tipoMaterial;

    /** Docente autor o responsable de la carga del material (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = true)
    private Docente docente;

    public Material() {
    }

    public Material(TipoMaterial tipoMaterial, String titulo, String rutaArchivo, Unidad unidad) {
        this.tipoMaterial = tipoMaterial;
        this.titulo = titulo;
        this.rutaArchivo = rutaArchivo;
        this.unidad = unidad;
        this.fechaCarga = LocalDateTime.now();
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

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public boolean isGeneradoPorIa() {
        return generadoPorIa;
    }

    public boolean getGeneradoPorIa() {
        return generadoPorIa;
    }

    public void setGeneradoPorIa(boolean generadoPorIa) {
        this.generadoPorIa = generadoPorIa;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public boolean getPublicado() {
        return publicado;
    }

    public void setPublicado(boolean publicado) {
        this.publicado = publicado;
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

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public TipoMaterial getTipoMaterial() {
        return tipoMaterial;
    }

    public void setTipoMaterial(TipoMaterial tipoMaterial) {
        this.tipoMaterial = tipoMaterial;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public boolean isGeneradoPorIA() {
        return generadoPorIa;
    }

    public void setGeneradoPorIA(boolean g) {
        this.generadoPorIa = g;
    }
}
