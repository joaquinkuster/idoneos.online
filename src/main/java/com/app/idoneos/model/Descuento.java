package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Descuento aplicable al precio de un curso.
 * cursosRequeridos: cantidad de cursos previos que el alumno debe tener
 * para acceder a este descuento (única condición de negocio existente).
 */
@Entity
@Table(name = "descuento")
@Getter @Setter
@NoArgsConstructor
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "porcentaje", nullable = false)
    private Double porcentaje;

    /**
     * DDL: vigencia_desde timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "vigencia_desde", nullable = false)
    private LocalDateTime vigenciaDesde;

    /**
     * DDL: vigencia_hasta timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    @Column(name = "cantidad_limite", nullable = true)
    private Integer cantidadLimite;

    @Column(name = "cantidad_usada", nullable = false)
    private Integer cantidadUsada = 0;

    @Column(name = "cursos_requeridos", nullable = false)
    private Integer cursosRequeridos = 0;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * DDL: fecha_creacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * DDL: ultima_modificacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "ultima_modificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(Double porcentaje) { this.porcentaje = porcentaje; }

    public LocalDateTime getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDateTime vigenciaDesde) { this.vigenciaDesde = vigenciaDesde; }

    public LocalDateTime getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDateTime vigenciaHasta) { this.vigenciaHasta = vigenciaHasta; }

    public Integer getCantidadLimite() { return cantidadLimite; }
    public void setCantidadLimite(Integer cantidadLimite) { this.cantidadLimite = cantidadLimite; }

    public Integer getCantidadUsada() { return cantidadUsada; }
    public void setCantidadUsada(Integer cantidadUsada) { this.cantidadUsada = cantidadUsada; }

    public Integer getCursosRequeridos() { return cursosRequeridos; }
    public void setCursosRequeridos(Integer cursosRequeridos) { this.cursosRequeridos = cursosRequeridos; }

    public Boolean getBaja() { return baja; }
    public void setBaja(Boolean baja) { this.baja = baja; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return !baja
            && !ahora.isBefore(vigenciaDesde)
            && !ahora.isAfter(vigenciaHasta)
            && (cantidadLimite == null || cantidadUsada < cantidadLimite);
    }
}
