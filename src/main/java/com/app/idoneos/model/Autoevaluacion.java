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
}
