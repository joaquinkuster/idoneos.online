package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clase_clon_ia")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaseClonIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "guion", nullable = false, columnDefinition = "text")
    private String guion;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_clase_clon_ia_id", nullable = false)
    private EstadoClaseClonIA estadoClaseClonIA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getGuion() { return guion; }
    public void setGuion(String guion) { this.guion = guion; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public EstadoClaseClonIA getEstadoClaseClonIA() { return estadoClaseClonIA; }
    public void setEstadoClaseClonIA(EstadoClaseClonIA estadoClaseClonIA) { this.estadoClaseClonIA = estadoClaseClonIA; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public ClaseClonIA(String titulo, Unidad unidad, Docente docente, EstadoClaseClonIA estadoClaseClonIA) {
        this.titulo = titulo;
        this.unidad = unidad;
        this.docente = docente;
        this.estadoClaseClonIA = estadoClaseClonIA;
        this.guion = "";
    }
    public EstadoClaseClonIA getEstado() { return estadoClaseClonIA; }
    public void setEstado(EstadoClaseClonIA estado) { this.estadoClaseClonIA = estado; }

}
