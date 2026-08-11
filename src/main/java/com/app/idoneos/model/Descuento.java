package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Descuento: Reglas promocionales o cupones aplicables al precio de
 * inscripción de un curso.
 * Mapea directamente a la tabla "Descuento" en base_datos.sql.
 */
@Entity
@Table(name = "Descuento")
public class Descuento {

    /** Identificador único del descuento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre de la promoción o cupón de descuento. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Requisito de cantidad de cursos previos comprados para aplicar. */
    @Column(name = "cursos_requeridos", nullable = false)
    private int cursosRequeridos = 0;

    /** Porcentaje de descuento porcentual a restar sobre el precio. */
    @Column(name = "porcentaje", nullable = false)
    private float porcentaje;

    /** Fecha y hora inicial de vigencia. */
    @Column(name = "vigencia_desde", nullable = false)
    private LocalDateTime vigenciaDesde;

    /** Fecha y hora final de vigencia. */
    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    /** Cantidad límite de usos totales permitidos. */
    @Column(name = "cantidad_limite", nullable = false)
    private int cantidadLimite;

    /** Contador de cantidad de veces que el descuento ya fue aplicado. */
    @Column(name = "cantidad_usada", nullable = false)
    private int cantidadUsada = 0;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación de datos. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del descuento. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    public Descuento() {
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

    public int getCursosRequeridos() {
        return cursosRequeridos;
    }

    public void setCursosRequeridos(int cursosRequeridos) {
        this.cursosRequeridos = cursosRequeridos;
    }

    public float getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(float porcentaje) {
        this.porcentaje = porcentaje;
    }

    public LocalDateTime getVigenciaDesde() {
        return vigenciaDesde;
    }

    public void setVigenciaDesde(LocalDateTime vigenciaDesde) {
        this.vigenciaDesde = vigenciaDesde;
    }

    public LocalDateTime getVigenciaHasta() {
        return vigenciaHasta;
    }

    public void setVigenciaHasta(LocalDateTime vigenciaHasta) {
        this.vigenciaHasta = vigenciaHasta;
    }

    public int getCantidadLimite() {
        return cantidadLimite;
    }

    public void setCantidadLimite(int cantidadLimite) {
        this.cantidadLimite = cantidadLimite;
    }

    public int getCantidadUsada() {
        return cantidadUsada;
    }

    public void setCantidadUsada(int cantidadUsada) {
        this.cantidadUsada = cantidadUsada;
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

    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return !baja && (vigenciaDesde == null || !ahora.isBefore(vigenciaDesde))
                && (vigenciaHasta == null || !ahora.isAfter(vigenciaHasta))
                && (cantidadLimite == 0 || cantidadUsada < cantidadLimite);
    }
}
