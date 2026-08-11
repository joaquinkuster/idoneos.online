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
}
