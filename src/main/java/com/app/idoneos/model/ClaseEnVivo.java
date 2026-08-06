package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Clase dictada en vivo por un docente.
 * Se relaciona con Unidad (obligatorio) y Docente (quien dicta).
 * La grabación resultante (Material) es opcional: se completa al finalizar la clase.
 */
@Entity
@Table(name = "clase_en_vivo")
@Getter @Setter
@NoArgsConstructor
public class ClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "url_rtmp", nullable = true, length = 255)
    private String urlRtmp;

    /**
     * Clave privada que autentica al docente frente al servidor de streaming.
     */
    @Column(name = "clave_stream", nullable = true, length = 100)
    private String claveStream;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /**
     * Grabación resultante. Null mientras la clase no haya finalizado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material", nullable = true)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoClaseEnVivo estado;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

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

    public Boolean getBaja() { return baja; }
    public void setBaja(Boolean baja) { this.baja = baja; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public EstadoClaseEnVivo getEstado() { return estado; }
    public void setEstado(EstadoClaseEnVivo estado) { this.estado = estado; }

    public ClaseEnVivo(String titulo, LocalDateTime fechaHora, Unidad unidad, Docente docente, EstadoClaseEnVivo estado) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.unidad = unidad;
        this.docente = docente;
        this.estado = estado;
    }
}
