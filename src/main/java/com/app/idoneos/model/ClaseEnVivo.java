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

    public ClaseEnVivo(String titulo, LocalDateTime fechaHora, Unidad unidad, Docente docente, EstadoClaseEnVivo estado) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.unidad = unidad;
        this.docente = docente;
        this.estado = estado;
    }
}
