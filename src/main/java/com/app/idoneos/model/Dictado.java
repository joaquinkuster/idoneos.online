package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dictado de clases programado de un Programa de curso.
 * Define cronograma (fecha inicio/fin), cupos y equipo docente asociado.
 */
@Entity
@Table(name = "dictado")
public class Dictado {

    /** Identificador único del dictado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Fecha de inicio de las clases de este dictado. */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    /** Fecha de finalización de las clases de este dictado. */
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /** Cupo máximo de alumnos (nulo si no hay límite). */
    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    /** Marca de baja lógica del dictado. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Programa al que pertenece este dictado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    /** Asignaciones docentes a este dictado (titulares y supervisores). */
    @OneToMany(mappedBy = "dictado", cascade = CascadeType.ALL)
    private List<DictadoDocente> dictadosDocentes = new ArrayList<>();

    /** Inscripciones de alumnos realizadas a este dictado puntual. */
    @OneToMany(mappedBy = "dictado", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    public Dictado() {}

    public Dictado(LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer cupoMaximo, Programa programa) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cupoMaximo = cupoMaximo;
        this.programa = programa;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Integer getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(Integer cupoMaximo) { this.cupoMaximo = cupoMaximo; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public Programa getPrograma() { return programa; }
    public void setPrograma(Programa programa) { this.programa = programa; }

    public List<DictadoDocente> getDictadosDocentes() { return dictadosDocentes; }
    public void setDictadosDocentes(List<DictadoDocente> dictadosDocentes) { this.dictadosDocentes = dictadosDocentes; }

    public List<Inscripcion> getInscripciones() { return inscripciones; }
    public void setInscripciones(List<Inscripcion> inscripciones) { this.inscripciones = inscripciones; }
}
