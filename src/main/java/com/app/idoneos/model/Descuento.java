package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "descuento")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "cursos_requeridos", nullable = false)
    private int cursosRequeridos;

    @Column(name = "porcentaje", nullable = false)
    private double porcentaje;

    @Column(name = "vigencia_desde", nullable = false)
    private LocalDateTime vigenciaDesde;

    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    @Column(name = "cantidad_limite", nullable = false)
    private int cantidadLimite;

    @Column(name = "cantidad_usada", nullable = false)
    private int cantidadUsada = 0;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @OneToMany(mappedBy = "descuento", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCursosRequeridos() { return cursosRequeridos; }
    public void setCursosRequeridos(int cursosRequeridos) { this.cursosRequeridos = cursosRequeridos; }

    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }

    public LocalDateTime getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDateTime vigenciaDesde) { this.vigenciaDesde = vigenciaDesde; }

    public LocalDateTime getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDateTime vigenciaHasta) { this.vigenciaHasta = vigenciaHasta; }

    public int getCantidadLimite() { return cantidadLimite; }
    public void setCantidadLimite(int cantidadLimite) { this.cantidadLimite = cantidadLimite; }

    public int getCantidadUsada() { return cantidadUsada; }
    public void setCantidadUsada(int cantidadUsada) { this.cantidadUsada = cantidadUsada; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public List<Inscripcion> getInscripciones() { return inscripciones; }
    public void setInscripciones(List<Inscripcion> inscripciones) { this.inscripciones = inscripciones; }


    public boolean estaVigente() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return !baja && (cantidadLimite <= 0 || cantidadUsada < cantidadLimite)
                && (vigenciaDesde == null || !now.isBefore(vigenciaDesde))
                && (vigenciaHasta == null || !now.isAfter(vigenciaHasta));
    }

}
