package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase generada con un clon de inteligencia artificial del docente a partir de un guion.
 */
@Entity
@Table(name = "ClaseClon")
@Getter
@Setter
@NoArgsConstructor
public class ClaseClon {

    /**
     * Identificador único de la clase con clon.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClaseClon")
    private int idClaseClon;

    /**
     * Relación con ParticipacionDocente.
     */
    @ManyToOne
    @JoinColumn(name = "idParticipacionDocente", nullable = false)
    private ParticipacionDocente participacionDocente;

    /**
     * Relación con EstadoClaseClon.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoClaseClon", nullable = false)
    private EstadoClaseClon estadoClaseClon;

    /**
     * Relación con Material. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idMaterial", nullable = true)
    private Material material;

    /**
     * Título de la clase.
     */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /**
     * Guion que el clon con inteligencia artificial debe recitar.
     */
    @Column(name = "guion", nullable = false, columnDefinition = "TEXT")
    private String guion;

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
     * Constructor para crear la clase con clon con los datos básicos.
     *
     * @param participacionDocente relación con ParticipacionDocente.
     * @param estadoClaseClon relación con EstadoClaseClon.
     * @param titulo título de la clase.
     * @param guion guion que el clon con inteligencia artificial debe recitar.
     */
    public ClaseClon(ParticipacionDocente participacionDocente, EstadoClaseClon estadoClaseClon, String titulo, String guion) {
        this.participacionDocente = participacionDocente;
        this.estadoClaseClon = estadoClaseClon;
        this.titulo = titulo;
        this.guion = guion;
    }

    /**
     * Devuelve una representación en forma de cadena de la clase con clon.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }
}
