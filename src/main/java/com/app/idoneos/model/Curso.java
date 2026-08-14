package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Curso: Ficha comercial y catálogo principal del curso ofrecido.
 * Mapea directamente a la tabla "Curso" en base_datos.sql.
 */
@Entity
@Table(name = "Curso")
public class Curso {

    /** Identificador único del curso. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre comercial del curso. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Descripción detallada del alcance académico. */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    /** Precio del curso (0 indica que es gratuito). */
    @Column(name = "precio", nullable = false)
    private float precio;

    /** Ruta de la imagen de portada. */
    @Column(name = "imagen", length = 150)
    private String imagen;

    /** Indicador de visibilidad pública en el catálogo general. */
    @Column(name = "publicado", nullable = false)
    private boolean publicado = false;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación de datos. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del curso. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Categoría temática a la que se adscribe el curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Programas o planes de estudio asociados. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private List<Programa> programas = new ArrayList<>();

    @Transient
    private int mesesAcceso = 12;

    public Curso() {
    }

    public Curso(String nombre, String descripcion, float precio, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
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

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Programa> getProgramas() {
        return programas;
    }

    public void setProgramas(List<Programa> programas) {
        this.programas = programas;
    }

    public int getMesesAcceso() {
        return mesesAcceso;
    }

    public void setMesesAcceso(int mesesAcceso) {
        this.mesesAcceso = mesesAcceso;
    }
}
