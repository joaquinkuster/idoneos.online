package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autoevaluacion")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "tiempo_limite", nullable = false)
    private int tiempoLimite;

    @Column(name = "intentos_permitidos")
    private Integer intentosPermitidos;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private List<PoolAutoevaluacion> poolsAutoevaluaciones = new ArrayList<>();

    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private List<IntentoAutoevaluacion> intentosAutoevaluacion = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getTiempoLimite() { return tiempoLimite; }
    public void setTiempoLimite(int tiempoLimite) { this.tiempoLimite = tiempoLimite; }

    public Integer getIntentosPermitidos() { return intentosPermitidos; }
    public void setIntentosPermitidos(Integer intentosPermitidos) { this.intentosPermitidos = intentosPermitidos; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public List<PoolAutoevaluacion> getPoolsAutoevaluaciones() { return poolsAutoevaluaciones; }
    public void setPoolsAutoevaluaciones(List<PoolAutoevaluacion> poolsAutoevaluaciones) { this.poolsAutoevaluaciones = poolsAutoevaluaciones; }

    public List<IntentoAutoevaluacion> getIntentosAutoevaluacion() { return intentosAutoevaluacion; }
    public void setIntentosAutoevaluacion(List<IntentoAutoevaluacion> intentosAutoevaluacion) { this.intentosAutoevaluacion = intentosAutoevaluacion; }


    public Autoevaluacion(String nombre, Pool pool, int tiempoLimite) {
        this.nombre = nombre;
        this.tiempoLimite = tiempoLimite;
        this.fechaApertura = java.time.LocalDateTime.now();
        // pool se vincula a través de PoolAutoevaluacion
    }
    /** Alias para compatibilidad con controllers que usaban getPools()/setPools(). */
    public java.util.List<PoolAutoevaluacion> getPools() { return poolsAutoevaluaciones; }
    public void setPools(java.util.List<Pool> pools) {
        if (pools != null) {
            this.poolsAutoevaluaciones = pools.stream().map(p -> {
                return new PoolAutoevaluacion(p, this);
            }).collect(java.util.stream.Collectors.toList());
        }
    }

}
