package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad ClaseEnVivo: Transmisión en vivo programada dentro de una Cohorte.
 * Mapea directamente a la tabla "ClaseEnVivo" en base_datos.sql.
 */
@Entity
@Table(name = "ClaseEnVivo")
public class ClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clase_en_vivo")
    private int idClaseEnVivo;

    /** Título de la clase en vivo. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Fecha y hora programada de inicio de la transmisión. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Duración estimada de la clase en minutos. */
    @Column(name = "duracion_estimada", nullable = false)
    private int duracionEstimada;

    /** URL RTMP de la transmisión (endpoint del servidor de streaming). */
    @Column(name = "url_rtmp", nullable = false, length = 255)
    private String urlRtmp;

    /** Clave de stream para autenticación en el servidor de transmisión. */
    @Column(name = "clave_stream", nullable = false, length = 100)
    private String claveStream;

    /** Indica si la clase está oculta para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private boolean oculto = false;

    /** Estado de baja lógica de la clase. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Docente que dicta la clase. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /** Estado de la clase (Programada, En Vivo, Finalizada, etc.). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_clase_en_vivo", nullable = false)
    private EstadoClaseEnVivo estadoClaseEnVivo;

    /** Material multimedia asociado (grabación generada, puede ser nulo). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    private Material material;

    /** Cohorte a la que pertenece esta clase en vivo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cohorte", nullable = false)
    private Cohorte cohorte;

    public ClaseEnVivo() {
    }

    public ClaseEnVivo(String titulo, LocalDateTime fechaHora, Docente docente, EstadoClaseEnVivo estadoClaseEnVivo, Cohorte cohorte) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.duracionEstimada = 60;
        this.urlRtmp = "";
        this.claveStream = "";
        this.docente = docente;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.cohorte = cohorte;
    }

    public ClaseEnVivo(String titulo, LocalDateTime fechaHora, int duracionEstimada,
                       String urlRtmp, String claveStream,
                       Docente docente, EstadoClaseEnVivo estadoClaseEnVivo, Cohorte cohorte) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.duracionEstimada = duracionEstimada;
        this.urlRtmp = urlRtmp;
        this.claveStream = claveStream;
        this.docente = docente;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.cohorte = cohorte;
    }

    public int getId() {
        return idClaseEnVivo;
    }

    public int getIdClaseEnVivo() {
        return idClaseEnVivo;
    }

    public void setId(int id) {
        this.idClaseEnVivo = id;
    }

    public void setIdClaseEnVivo(int idClaseEnVivo) {
        this.idClaseEnVivo = idClaseEnVivo;
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

    public int getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(int duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
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

    public boolean isOculto() {
        return oculto;
    }

    public boolean getOculto() {
        return oculto;
    }

    public void setOculto(boolean oculto) {
        this.oculto = oculto;
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

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public EstadoClaseEnVivo getEstadoClaseEnVivo() {
        return estadoClaseEnVivo;
    }

    public void setEstadoClaseEnVivo(EstadoClaseEnVivo estadoClaseEnVivo) {
        this.estadoClaseEnVivo = estadoClaseEnVivo;
    }

    public EstadoClaseEnVivo getEstado() {
        return estadoClaseEnVivo;
    }

    public void setEstado(EstadoClaseEnVivo estado) {
        this.estadoClaseEnVivo = estado;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Cohorte getCohorte() {
        return cohorte;
    }

    public void setCohorte(Cohorte cohorte) {
        this.cohorte = cohorte;
    }

    /**
     * Helper para obtener la Unidad asociada a la clase en vivo a través del cronograma de su cohorte/programa.
     */
    public Unidad getUnidad() {
        if (this.material != null && this.material.getUnidad() != null) {
            return this.material.getUnidad();
        }
        if (this.cohorte != null && this.cohorte.getPrograma() != null && this.cohorte.getPrograma().getCronogramas() != null) {
            for (Cronograma c : this.cohorte.getPrograma().getCronogramas()) {
                if (c.getUnidad() != null) {
                    return c.getUnidad();
                }
            }
        }
        return null;
    }

    /**
     * Helper para obtener el Curso asociado a la clase en vivo.
     */
    public Curso getCurso() {
        if (this.cohorte != null && this.cohorte.getPrograma() != null) {
            return this.cohorte.getPrograma().getCurso();
        }
        return null;
    }
}

