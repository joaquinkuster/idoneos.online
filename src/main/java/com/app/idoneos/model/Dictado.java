package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dictado")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dictado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @OneToMany(mappedBy = "dictado", cascade = CascadeType.ALL)
    private List<DictadoDocente> dictadosDocentes = new ArrayList<>();

    @OneToMany(mappedBy = "dictado", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones = new ArrayList<>();
}
