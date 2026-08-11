package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "material")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "ruta_archivo", length = 150)
    private String rutaArchivo;

    @Column(name = "contenido", length = 500)
    private String contenido;

    @Column(name = "duracion")
    private Integer duracion;

    @Column(name = "autor", length = 50)
    private String autor;

    @Column(name = "generado_por_ia", nullable = false)
    private boolean generadoPorIa = false;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "publicado", nullable = false)
    private boolean publicado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_material_id", nullable = false)
    private TipoMaterial tipoMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();

    public Material(TipoMaterial tipoMaterial, String titulo, String contenido, Unidad unidad) {
        this.tipoMaterial = tipoMaterial;
        this.titulo = titulo;
        this.contenido = contenido;
        this.unidad = unidad;
    }
}
