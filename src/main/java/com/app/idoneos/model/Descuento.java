package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Descuento: Código o regla de descuento aplicable a pagos.
 * Mapea directamente a la tabla "Descuento" en base_datos.sql.
 */
@Entity
@Table(name = "Descuento")
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_descuento")
    private int idDescuento;

    /** Nombre o código identificatorio del descuento. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Cantidad de cursos previos requeridos para aplicar el descuento. */
    @Column(name = "cursos_requeridos", nullable = false)
    private int cursosRequeridos;

    /** Porcentaje de reducción sobre el precio (ej. 10.0 = 10%). */
    @Column(name = "porcentaje", nullable = false)
    private float porcentaje;

    /** Fecha desde la cual el descuento es válido. */
    @Column(name = "vigencia_desde", nullable = false)
    private LocalDateTime vigenciaDesde;

    /** Fecha hasta la cual el descuento es válido. */
    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    /** Cantidad máxima de usos disponibles para el descuento. */
    @Column(name = "cantidad_limite", nullable = false)
    private int cantidadLimite;

    /** Cantidad de veces que el descuento ya ha sido utilizado. */
    @Column(name = "cantidad_usada", nullable = false)
    private int cantidadUsada = 0;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación. */
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    /** Estado de baja lógica del descuento. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    public Descuento() {
    }

    public int getId() {
        return idDescuento;
    }

    public int getIdDescuento() {
        return idDescuento;
    }

    public void setId(int id) {
        this.idDescuento = id;
    }

    public void setIdDescuento(int idDescuento) {
        this.idDescuento = idDescuento;
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

    /**
     * Helper para verificar si el descuento está vigente y disponible.
     */
    public boolean estaVigente() {
        if (baja) return false;
        LocalDateTime ahora = LocalDateTime.now();
        if (vigenciaDesde != null && ahora.isBefore(vigenciaDesde)) return false;
        if (vigenciaHasta != null && ahora.isAfter(vigenciaHasta)) return false;
        if (cantidadLimite > 0 && cantidadUsada >= cantidadLimite) return false;
        return true;
    }
}

