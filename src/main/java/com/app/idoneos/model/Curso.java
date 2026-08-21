package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Curso: Oferta educativa principal de la plataforma.
 * Mapea directamente a la tabla "Curso" en base_datos.sql.
 */
@Entity
@Table(name = "Curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private int idCurso;

    /** Nombre del curso. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Descripción breve del curso. */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    /** Precio base del curso. */
    @Column(name = "precio", nullable = false)
    private float precio;

    /** Ruta de la imagen de portada del curso. */
    @Column(name = "imagen", length = 150)
    private String imagen;

    /** Indica si el curso emite certificado de aprobación. */
    @Column(name = "emite_certificado", nullable = false)
    private boolean emiteCertificado = false;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del curso. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Categoría temática del curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    /** Nivel de dificultad del curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel", nullable = false)
    private Nivel nivel;

    /** Docente responsable del curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /** Programas de estudio asociados a este curso. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private List<Programa> programas = new ArrayList<>();

    public Curso() {
    }

    public Curso(String nombre, String descripcion, float precio, Categoria categoria,
                 Nivel nivel, Docente docente) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.nivel = nivel;
        this.docente = docente;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idCurso;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setId(int id) {
        this.idCurso = id;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
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

    public boolean isEmiteCertificado() {
        return emiteCertificado;
    }

    public boolean getEmiteCertificado() {
        return emiteCertificado;
    }

    public void setEmiteCertificado(boolean emiteCertificado) {
        this.emiteCertificado = emiteCertificado;
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

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public List<Programa> getProgramas() {
        return programas;
    }

    public void setProgramas(List<Programa> programas) {
        this.programas = programas;
    }
}
