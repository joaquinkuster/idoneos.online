package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase dictada en vivo para una cohorte, con transmisión mediante RTMP.
 */
@Entity
@Table(name = "ClaseEnVivo")
@Getter
@Setter
@NoArgsConstructor
public class ClaseEnVivo {

    /**
     * Identificador único de la clase en vivo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClaseEnVivo")
    private int idClaseEnVivo;

    /**
     * Relación con ParticipacionDocente.
     */
    @ManyToOne
    @JoinColumn(name = "idParticipacionDocente", nullable = false)
    private ParticipacionDocente participacionDocente;

    /**
     * Relación con EstadoClaseEnVivo.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoClaseEnVivo", nullable = false)
    private EstadoClaseEnVivo estadoClaseEnVivo;

    /**
     * Relación con Material. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idMaterial", nullable = true)
    private Material material;

    /**
     * Relación con Cohorte.
     */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    /**
     * Título de la clase.
     */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /**
     * Fecha y hora en que se dicta la clase.
     */
    @Column(name = "fechaHora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Duración de la clase, en minutos.
     */
    @Column(name = "duracion", nullable = false)
    private int duracion;

    /**
     * URL del servidor RTMP para transmitir la clase. Es opcional.
     */
    @Column(name = "urlRtmp", nullable = true, length = 255)
    private String urlRtmp;

    /**
     * Clave de transmisión de la clase. Es opcional.
     */
    @Column(name = "claveStream", nullable = true, length = 100, unique = true)
    private String claveStream;

    /**
     * Indica si el elemento está oculto para los alumnos.
     */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Constructor para crear la clase en vivo con los datos básicos.
     *
     * @param participacionDocente relación con ParticipacionDocente.
     * @param estadoClaseEnVivo relación con EstadoClaseEnVivo.
     * @param cohorte relación con Cohorte.
     * @param titulo título de la clase.
     * @param fechaHora fecha y hora en que se dicta la clase.
     * @param duracion duración de la clase, en minutos.
     */
    public ClaseEnVivo(ParticipacionDocente participacionDocente, EstadoClaseEnVivo estadoClaseEnVivo, Cohorte cohorte, String titulo, LocalDateTime fechaHora, int duracion) {
        this.participacionDocente = participacionDocente;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.cohorte = cohorte;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.duracion = duracion;
    }

    /**
     * Devuelve una representación en forma de cadena de la clase en vivo.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }
}
