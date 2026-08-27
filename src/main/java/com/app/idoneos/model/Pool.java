package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Pool: Banco de preguntas de evaluación asociado a una Unidad temática.
 * Mapea directamente a la tabla "Pool" en base_datos.sql.
 */
@Entity
@Table(name = "Pool")
public class Pool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pool")
    private int idPool;

    /** Nombre identificatorio del banco de preguntas. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Fecha de creación del pool. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación del registro. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del pool. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática a la que pertenece el pool. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public Pool() {
    }

    public Pool(String nombre, Unidad unidad) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() {
        return idPool;
    }

    public int getIdPool() {
        return idPool;
    }

    public void setId(int id) {
        this.idPool = id;
    }

    public void setIdPool(int idPool) {
        this.idPool = idPool;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}

