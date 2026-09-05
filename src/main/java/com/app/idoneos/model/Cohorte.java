package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Cohorte: Edición o grupo de cursado de un Programa en un período determinado.
 * Mapea directamente a la tabla "Cohorte" en base_datos.sql.
 */
@Entity
@Table(name = "Cohorte")
public class Cohorte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cohorte")
    private int idCohorte;

    /** Fecha de inicio del período de inscripción a la cohorte. */
    @Column(name = "fecha_inicio_inscripcion", nullable = false)
    private LocalDateTime fechaInicioInscripcion;

    /** Fecha de fin del período de inscripción. */
    @Column(name = "fecha_fin_inscripcion", nullable = false)
    private LocalDateTime fechaFinInscripcion;

    /** Fecha de inicio de las clases del dictado. */
    @Column(name = "fecha_inicio_dictado")
    private LocalDateTime fechaInicioDictado;

    /** Fecha de fin de las clases del dictado. */
    @Column(name = "fecha_fin_dictado")
    private LocalDateTime fechaFinDictado;

    /** Cupo máximo de alumnos permitidos en la cohorte (null = sin límite). */
    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    /** Semanas de acceso a los materiales luego de finalizado el dictado. */
    @Column(name = "semanas_acceso", nullable = false)
    private int semanasAcceso;

    /** Estado de baja lógica de la cohorte. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Programa al que pertenece esta cohorte. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_programa", nullable = false)
    private Programa programa;

    public Cohorte() {
    }

    public Cohorte(LocalDateTime fechaInicioInscripcion, LocalDateTime fechaFinInscripcion,
                   int semanasAcceso, Programa programa) {
        this.fechaInicioInscripcion = fechaInicioInscripcion;
        this.fechaFinInscripcion = fechaFinInscripcion;
        this.semanasAcceso = semanasAcceso;
        this.programa = programa;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idCohorte;
    }

    public int getIdCohorte() {
        return idCohorte;
    }

    public void setId(int id) {
        this.idCohorte = id;
    }

    public void setIdCohorte(int idCohorte) {
        this.idCohorte = idCohorte;
    }

    public LocalDateTime getFechaInicioInscripcion() {
        return fechaInicioInscripcion;
    }

    public void setFechaInicioInscripcion(LocalDateTime fechaInicioInscripcion) {
        this.fechaInicioInscripcion = fechaInicioInscripcion;
    }

    public LocalDateTime getFechaFinInscripcion() {
        return fechaFinInscripcion;
    }

    public void setFechaFinInscripcion(LocalDateTime fechaFinInscripcion) {
        this.fechaFinInscripcion = fechaFinInscripcion;
    }

    public LocalDateTime getFechaInicioDictado() {
        return fechaInicioDictado;
    }

    public void setFechaInicioDictado(LocalDateTime fechaInicioDictado) {
        this.fechaInicioDictado = fechaInicioDictado;
    }

    public LocalDateTime getFechaFinDictado() {
        return fechaFinDictado;
    }

    public void setFechaFinDictado(LocalDateTime fechaFinDictado) {
        this.fechaFinDictado = fechaFinDictado;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public int getSemanasAcceso() {
        return semanasAcceso;
    }

    public void setSemanasAcceso(int semanasAcceso) {
        this.semanasAcceso = semanasAcceso;
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

    public Programa getPrograma() {
        return programa;
    }

    public void setPrograma(Programa programa) {
        this.programa = programa;
    }

    /**
     * Helper para obtener un nombre descriptivo de la cohorte.
     */
    public String getNombre() {
        if (this.programa != null && this.programa.getNombre() != null) {
            return this.programa.getNombre();
        }
        return "Cohorte #" + this.idCohorte;
    }
}

