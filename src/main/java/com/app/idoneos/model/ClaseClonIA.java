package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Clase generada con avatar IA (HeyGen) a partir de un guion (no persistido).
 * Estado asincrónico: Pendiente → Generada | Error.
 */
@Entity
@Table(name = "clases_clon_ia")
@Getter @Setter
@NoArgsConstructor
public class ClaseClonIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /**
     * DDL: fecha_generacion timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_generacion", nullable = true)
    private LocalDateTime fechaGeneracion;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /**
     * Video resultante. Null mientras la generación está pendiente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material", nullable = true)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoClaseClonIA estado;

    public ClaseClonIA(String titulo, Unidad unidad, Docente docente, EstadoClaseClonIA estado) {
        this.titulo = titulo;
        this.unidad = unidad;
        this.docente = docente;
        this.estado = estado;
    }
}
