package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Intento de un alumno de resolver una autoevaluación, con su nota y resultado.
 */
@Entity
@Table(name = "IntentoAutoevaluacion")
@Getter
@Setter
@NoArgsConstructor
public class IntentoAutoevaluacion {

    /**
     * Identificador único del intento de autoevaluación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIntentoAutoevaluacion")
    private int idIntentoAutoevaluacion;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Relación con Autoevaluacion.
     */
    @ManyToOne
    @JoinColumn(name = "idAutoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    /**
     * Nota obtenida. Es opcional hasta que se entrega el intento.
     */
    @Column(name = "nota", nullable = true)
    private Float nota;

    /**
     * Indica si el intento fue aprobado.
     */
    @Column(name = "aprobado", nullable = false)
    private Boolean aprobado = false;

    /**
     * Fecha y hora de entrega del intento. Es opcional.
     */
    @Column(name = "fechaEntrega", nullable = true)
    private LocalDateTime fechaEntrega;

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
     * Conjunto de respuestasIntento asociados.
     */
    @OneToMany(mappedBy = "intentoAutoevaluacion", cascade = CascadeType.ALL)
    private Set<RespuestaIntento> respuestasIntento = new HashSet<>();

    /**
     * Constructor para crear el intento de autoevaluación con los datos básicos.
     *
     * @param inscripcion relación con Inscripcion.
     * @param autoevaluacion relación con Autoevaluacion.
     */
    public IntentoAutoevaluacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion) {
        this.inscripcion = inscripcion;
        this.autoevaluacion = autoevaluacion;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena del intento de autoevaluación.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "IntentoAutoevaluacion #" + idIntentoAutoevaluacion;
    }
}
