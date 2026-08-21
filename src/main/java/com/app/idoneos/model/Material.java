package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Material: Contenido multimedia o de lectura asociado a una Unidad.
 * Mapea directamente a la tabla "Material" en base_datos.sql.
 */
@Entity
@Table(name = "Material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private int idMaterial;

    /** Título del material. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Ruta del archivo en el sistema de almacenamiento. */
    @Column(name = "ruta_archivo", length = 150)
    private String rutaArchivo;

    /** Contenido textual o resumen del material. */
    @Column(name = "contenido", length = 500)
    private String contenido;

    /** Duración del material en minutos (para videos/audio). */
    @Column(name = "duracion")
    private Integer duracion;

    /** Nombre del autor o fuente del material. */
    @Column(name = "autor", length = 50)
    private String autor;

    /** Indica si el material fue generado por IA. */
    @Column(name = "generado_por_ia", nullable = false)
    private boolean generadoPorIa = false;

    /** Indica si el material está oculto para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private boolean oculto = false;

    /** Fecha de creación del material. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación del registro. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del material. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Docente responsable del material. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /** Tipo de material (Grabación, Bibliografía, Presentación, Resumen). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_material", nullable = false)
    private TipoMaterial tipoMaterial;

    /** Unidad temática a la que pertenece el material. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public Material() {
    }

    public Material(String titulo, Docente docente, TipoMaterial tipoMaterial, Unidad unidad) {
        this.titulo = titulo;
        this.docente = docente;
        this.tipoMaterial = tipoMaterial;
        this.unidad = unidad;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idMaterial;
    }

    public int getIdMaterial() {
        return idMaterial;
    }

    public void setId(int id) {
        this.idMaterial = id;
    }

    public void setIdMaterial(int idMaterial) {
        this.idMaterial = idMaterial;
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

    public boolean isOculto() {
        return oculto;
    }

    public boolean getOculto() {
        return oculto;
    }

    public void setOculto(boolean oculto) {
        this.oculto = oculto;
    }

    /** Alias de compatibilidad: getter de oculto con nombre anterior 'publicado'. */
    public boolean isPublicado() {
        return !oculto;
    }

    public boolean getPublicado() {
        return !oculto;
    }

    public void setGeneradoPorIA(boolean generado) {
        this.generadoPorIa = generado;
    }

    /** Alias de compatibilidad: setter de publicado (invertido → oculto). */
    public void setPublicado(boolean publicado) {
        this.oculto = !publicado;
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

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public TipoMaterial getTipoMaterial() {
        return tipoMaterial;
    }

    public void setTipoMaterial(TipoMaterial tipoMaterial) {
        this.tipoMaterial = tipoMaterial;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }
}
