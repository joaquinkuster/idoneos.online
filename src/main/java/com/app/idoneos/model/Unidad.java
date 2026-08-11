package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unidad")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "numero_orden", nullable = false)
    private int numeroOrden;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Material> materiales = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ConsultaForo> consultasForo = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Autoevaluacion> autoevaluaciones = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Pool> pools = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Progreso> progresos = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<TerminoGlosario> terminosGlosario = new ArrayList<>();

    public Unidad(String titulo, String descripcion, int numeroOrden, Programa programa) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroOrden = numeroOrden;
        this.programa = programa;
    }
}
