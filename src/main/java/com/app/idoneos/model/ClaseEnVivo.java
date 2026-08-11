package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad ClaseEnVivo: Transmisión en directo vía streaming (RTMP/OBS) para una
 * unidad.
 * Mapea directamente a la tabla "ClaseEnVivo" en base_datos.sql.
 */
@Entity
@Table(name = "ClaseEnVivo")
public class ClaseEnVivo {

    /** Identificador único de la clase en vivo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Título descriptivo de la sesión en vivo. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Fecha y hora programada o en la que se dictó la clase. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** URL del servidor de transmisión RTMP. */
    @Column(name = "url_rtmp", nullable = false, length = 255)
    private String urlRtmp;

    /** Clave privada de acceso al stream. */
    @Column(name = "clave_stream", nullable = false, length = 100)
    private String claveStream;

    /** Estado de baja lógica de la clase. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Estado de la transmisión (Programada, En vivo, Finalizada). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_clase_en_vivo_id")
    private EstadoClaseEnVivo estadoClaseEnVivo;

    /** Grabación resultante asociada tras finalizar la sesión (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    /** Unidad a la que se adscribe la transmisión en vivo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    /** Docente a cargo del dictado en vivo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    public ClaseEnVivo() {
    }

    public ClaseEnVivo(String titulo, LocalDateTime fechaHora, Unidad unidad, Docente docente,
            EstadoClaseEnVivo estadoClaseEnVivo) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.unidad = unidad;
        this.docente = docente;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.urlRtmp = "rtmp://localhost/live";
        this.claveStream = "stream_" + System.currentTimeMillis();
    }

    public void setEstado(EstadoClaseEnVivo estado) {
        this.estadoClaseEnVivo = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUrlRtmp() {
        return urlRtmp;
    }

    public void setUrlRtmp(String urlRtmp) {
        this.urlRtmp = urlRtmp;
    }

    public String getClaveStream() {
        return claveStream;
    }

    public void setClaveStream(String claveStream) {
        this.claveStream = claveStream;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public EstadoClaseEnVivo getEstadoClaseEnVivo() {
        return estadoClaseEnVivo;
    }

    public void setEstadoClaseEnVivo(EstadoClaseEnVivo estadoClaseEnVivo) {
        this.estadoClaseEnVivo = estadoClaseEnVivo;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }
}
