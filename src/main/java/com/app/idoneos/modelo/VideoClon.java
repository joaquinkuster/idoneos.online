package com.app.idoneos.modelo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos propios de un recurso que es un video con el clon de inteligencia artificial del docente.
 */
@Entity
@Table(name = "VideoClon")
@Getter
@Setter
@NoArgsConstructor
public class VideoClon {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVideoClon")
    private int idVideoClon;

    /**
     * Relación con Recurso: el recurso del que esta tabla guarda los datos propios.
     */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /**
     * Relación con EstadoGeneracion.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoGeneracion", nullable = false)
    private EstadoGeneracion estadoGeneracion;

    /**
     * Identificador del video en el servicio de generación. Es opcional.
     */
    @Column(name = "idVideoExterno", nullable = true, length = 255)
    private String idVideoExterno;

    /**
     * Guion que dice el clon.
     */
    @Column(name = "guion", nullable = false, length = 500)
    private String guion;

    /**
     * Ruta del archivo de video generado. Es opcional.
     */
    @Column(name = "rutaArchivo", nullable = true, length = 500)
    private String rutaArchivo;

    /**
     * Tamaño del archivo en bytes. Es opcional.
     */
    @Column(name = "tamanioBytes", nullable = true)
    private Long tamanioBytes;

    /**
     * Duración del video en minutos. Es opcional.
     */
    @Column(name = "duracionMinutos", nullable = true)
    private Integer duracionMinutos;

    /**
     * Detalle del error si la generación falló. Es opcional.
     */
    @Column(name = "detalleError", nullable = true, length = 500)
    private String detalleError;

    /**
     * Constructor para crear el video con clon con los datos básicos.
     *
     * @param recurso          el recurso.
     * @param estadoGeneracion el estado de la generación.
     * @param guion            el guion.
     */
    public VideoClon(Recurso recurso, EstadoGeneracion estadoGeneracion, String guion) {
        this.recurso = recurso;
        this.estadoGeneracion = estadoGeneracion;
        this.guion = guion;
    }
}
