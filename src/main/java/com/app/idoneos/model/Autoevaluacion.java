package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autoevaluación: examen que sortea preguntas de uno o más Pools.
 * Un alumno puede tener múltiples intentos hasta agotar intentosPermitidos.
 */
@Entity
@Table(name = "autoevaluacion")
@Getter @Setter
@NoArgsConstructor
public class Autoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /**
     * Minutos que tiene el alumno para completar el intento.
     */
    @Column(name = "tiempo_limite", nullable = true)
    private Integer tiempoLimite;

    /**
     * Cantidad máxima de intentos permitidos por alumno.
     */
    @Column(name = "intentos_permitidos", nullable = false)
    private Integer intentosPermitidos = 3;

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

    @ManyToMany
    @JoinTable(
        name = "pool_autoevaluacion",
        joinColumns = @JoinColumn(name = "id_autoevaluacion"),
        inverseJoinColumns = @JoinColumn(name = "id_pool")
    )
    private List<Pool> pools = new ArrayList<>();

    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private List<IntentoAutoevaluacion> intentos = new ArrayList<>();

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getTiempoLimite() { return tiempoLimite; }
    public void setTiempoLimite(Integer tiempoLimite) { this.tiempoLimite = tiempoLimite; }

    public Integer getIntentosPermitidos() { return intentosPermitidos; }
    public void setIntentosPermitidos(Integer intentosPermitidos) { this.intentosPermitidos = intentosPermitidos; }

    public Boolean getBaja() { return baja; }
    public void setBaja(Boolean baja) { this.baja = baja; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public List<Pool> getPools() { return pools; }
    public void setPools(List<Pool> pools) { this.pools = pools; }

    public List<IntentoAutoevaluacion> getIntentos() { return intentos; }
    public void setIntentos(List<IntentoAutoevaluacion> intentos) { this.intentos = intentos; }

    public Autoevaluacion(String nombre, Integer tiempoLimite, Integer intentosPermitidos) {
        this.nombre = nombre;
        this.tiempoLimite = tiempoLimite;
        this.intentosPermitidos = intentosPermitidos;
    }
}
