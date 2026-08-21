package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Programa: Versión del plan de estudios de un Curso.
 * Las Unidades se vinculan al Programa a través de la entidad Cronograma.
 * Mapea directamente a la tabla "Programa" en base_datos.sql.
 */
@Entity
@Table(name = "Programa")
public class Programa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programa")
    private int idPrograma;

    /** Nombre de la versión del programa (ej. "Edición 2026"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Descripción sintética del plan de estudios. */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    /** Objetivos de aprendizaje del programa. */
    @Column(name = "objetivos", nullable = false, columnDefinition = "text")
    private String objetivos;

    /** Carga horaria total estimada en horas. */
    @Column(name = "carga_horaria_total")
    private Integer cargaHorariaTotal;

    /** Bibliografía recomendada del programa. */
    @Column(name = "bibliografia", nullable = false, columnDefinition = "text")
    private String bibliografia;

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
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    /** Cohortes (dictados) organizados bajo este programa. */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private List<Cohorte> cohortes = new ArrayList<>();

    /** Entradas del cronograma que vinculan unidades a este programa. */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private List<Cronograma> cronogramas = new ArrayList<>();

    public Programa() {
    }

    public Programa(String nombre, String descripcion, String objetivos, String bibliografia,
                    Curso curso) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.objetivos = objetivos;
        this.bibliografia = bibliografia;
        this.curso = curso;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idPrograma;
    }

    public int getIdPrograma() {
        return idPrograma;
    }

    public void setId(int id) {
        this.idPrograma = id;
    }

    public void setIdPrograma(int idPrograma) {
        this.idPrograma = idPrograma;
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

    public String getObjetivos() {
        return objetivos;
    }

    public void setObjetivos(String objetivos) {
        this.objetivos = objetivos;
    }

    public Integer getCargaHorariaTotal() {
        return cargaHorariaTotal;
    }

    public void setCargaHorariaTotal(Integer cargaHorariaTotal) {
        this.cargaHorariaTotal = cargaHorariaTotal;
    }

    public String getBibliografia() {
        return bibliografia;
    }

    public void setBibliografia(String bibliografia) {
        this.bibliografia = bibliografia;
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

    public List<Cohorte> getCohortes() {
        return cohortes;
    }

    public void setCohortes(List<Cohorte> cohortes) {
        this.cohortes = cohortes;
    }

    public List<Cronograma> getCronogramas() {
        return cronogramas;
    }

    public void setCronogramas(List<Cronograma> cronogramas) {
        this.cronogramas = cronogramas;
    }
}
