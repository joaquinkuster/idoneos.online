package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clase_en_vivo")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "url_rtmp", nullable = false, length = 255)
    private String urlRtmp;

    @Column(name = "clave_stream", nullable = false, length = 100)
    private String claveStream;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_clase_en_vivo_id", nullable = false)
    private EstadoClaseEnVivo estadoClaseEnVivo;

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

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getUrlRtmp() { return urlRtmp; }
    public void setUrlRtmp(String urlRtmp) { this.urlRtmp = urlRtmp; }

    public String getClaveStream() { return claveStream; }
    public void setClaveStream(String claveStream) { this.claveStream = claveStream; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public EstadoClaseEnVivo getEstadoClaseEnVivo() { return estadoClaseEnVivo; }
    public void setEstadoClaseEnVivo(EstadoClaseEnVivo estadoClaseEnVivo) { this.estadoClaseEnVivo = estadoClaseEnVivo; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public ClaseEnVivo(String titulo, LocalDateTime fechaHora, Unidad unidad, Docente docente, EstadoClaseEnVivo estadoClaseEnVivo) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.unidad = unidad;
        this.docente = docente;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.urlRtmp = "";
        this.claveStream = "";
    }
    public EstadoClaseEnVivo getEstado() { return estadoClaseEnVivo; }
    public void setEstado(EstadoClaseEnVivo estado) { this.estadoClaseEnVivo = estado; }

}
