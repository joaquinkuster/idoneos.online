package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Autoevaluacion: Instrumento de evaluación de conocimientos asociado a una Unidad.
 * Mapea directamente a la tabla "Autoevaluacion" en base_datos.sql.
 */
@Entity
@Table(name = "Autoevaluacion")
public class Autoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autoevaluacion")
    private int idAutoevaluacion;

    /** Nombre identificatorio de la autoevaluación. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Tiempo límite para completar la evaluación (en minutos). */
    @Column(name = "tiempo_limite", nullable = false)
    private int tiempoLimite;

    /** Cantidad máxima de intentos permitidos (null = ilimitados). */
    @Column(name = "intentos_permitidos")
    private Integer intentosPermitidos;

    /** Fecha y hora de apertura de la evaluación. */
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    /** Fecha y hora de cierre (null = sin fecha límite). */
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    /** Indica si la autoevaluación está oculta para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private boolean oculto = false;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica de la autoevaluación. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática a la que pertenece esta autoevaluación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public Autoevaluacion() {
    }

    public Autoevaluacion(String nombre, int tiempoLimite, LocalDateTime fechaApertura,
                          Unidad unidad) {
        this.nombre = nombre;
        this.tiempoLimite = tiempoLimite;
        this.fechaApertura = fechaApertura;
        this.unidad = unidad;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idAutoevaluacion;
    }

    public int getIdAutoevaluacion() {
        return idAutoevaluacion;
    }

    public void setId(int id) {
        this.idAutoevaluacion = id;
    }

    public void setIdAutoevaluacion(int idAutoevaluacion) {
        this.idAutoevaluacion = idAutoevaluacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTiempoLimite() {
        return tiempoLimite;
    }

    public void setTiempoLimite(int tiempoLimite) {
        this.tiempoLimite = tiempoLimite;
    }

    public Integer getIntentosPermitidos() {
        return intentosPermitidos;
    }

    public void setIntentosPermitidos(Integer intentosPermitidos) {
        this.intentosPermitidos = intentosPermitidos;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
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

    /**
     * Helper para obtener los pools asociados a la autoevaluación (de la unidad a la que pertenece).
     */
    public List<PoolAutoevaluacion> getPools() {
        if (this.unidad != null && this.unidad.getPools() != null) {
            return this.unidad.getPools().stream()
                    .map(p -> new PoolAutoevaluacion(p, this))
                    .toList();
        }
        return List.of();
    }
}
