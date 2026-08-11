package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Autoevaluación: Examen o prueba rrendible asociada a pools de
 * preguntas de una Unidad.
 * Mapea directamente a la tabla "Autoevaluacion" en base_datos.sql.
 */
@Entity
@Table(name = "Autoevaluacion")
public class Autoevaluacion {

    /** Identificador único de la autoevaluación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre comercial o título de la evaluación. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Tiempo límite en minutos para completar cada intento. */
    @Column(name = "tiempo_limite", nullable = false)
    private int tiempoLimite;

    /**
     * Cantidad máxima de intentos permitidos por alumno (nulo si no hay límite).
     */
    @Column(name = "intentos_permitidos")
    private Integer intentosPermitidos;

    /**
     * Fecha y hora a partir de la cual la autoevaluación se encuentra habilitada.
     */
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura = LocalDateTime.now();

    /** Fecha y hora límite hasta la cual se permiten nuevos intentos (opcional). */
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    /** Fecha de creación de la autoevaluación. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de última actualización de datos. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica de la autoevaluación. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática de la cual cuelga esta autoevaluación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    /** Vínculos asociativos M a N con Pools de preguntas. */
    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private List<PoolAutoevaluacion> pools = new ArrayList<>();

    public Autoevaluacion() {
    }

    public Autoevaluacion(String nombre, int tiempoLimite, Integer intentosPermitidos, Unidad unidad) {
        this.nombre = nombre;
        this.tiempoLimite = tiempoLimite;
        this.intentosPermitidos = intentosPermitidos;
        this.unidad = unidad;
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

    public List<PoolAutoevaluacion> getPools() {
        return pools;
    }

    public void setPools(List<PoolAutoevaluacion> pools) {
        this.pools = pools;
    }
}
