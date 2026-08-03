package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Autoevaluación: examen que sortea preguntas de uno o más Pools.
 * Un alumno puede tener múltiples intentos hasta agotar intentosPermitidos.
 */
@Entity
@Table(name = "autoevaluaciones")
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

    @ManyToMany
    @JoinTable(
        name = "pool_autoevaluacion",
        joinColumns = @JoinColumn(name = "id_autoevaluacion"),
        inverseJoinColumns = @JoinColumn(name = "id_pool")
    )
    private List<Pool> pools = new ArrayList<>();

    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private List<IntentoAutoevaluacion> intentos = new ArrayList<>();

    public Autoevaluacion(String nombre, Integer tiempoLimite, Integer intentosPermitidos) {
        this.nombre = nombre;
        this.tiempoLimite = tiempoLimite;
        this.intentosPermitidos = intentosPermitidos;
    }
}
