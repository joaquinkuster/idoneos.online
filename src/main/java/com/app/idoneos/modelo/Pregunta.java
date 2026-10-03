package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pregunta de un pool, con opciones de respuesta.
 */
@Entity
@Table(name = "Pregunta")
@Getter
@Setter
@NoArgsConstructor
public class Pregunta {

    /**
     * Identificador único de la pregunta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPregunta")
    private int idPregunta;

    /**
     * Relación con Pool.
     */
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /**
     * Texto de la pregunta.
     */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /**
     * Indica si la pregunta admite más de una opción correcta.
     */
    @Column(name = "esOpcionMultiple", nullable = false)
    private Boolean esOpcionMultiple = false;

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
     * Conjunto de opcionesRespuesta asociados.
     */
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private Set<OpcionRespuesta> opcionesRespuesta = new HashSet<>();

    /**
     * Constructor para crear la pregunta con los datos básicos.
     *
     * @param pool relación con Pool.
     * @param texto texto de la pregunta.
     * @param esOpcionMultiple indica si la pregunta admite más de una opción correcta.
     */
    public Pregunta(Pool pool, String texto, Boolean esOpcionMultiple) {
        this.pool = pool;
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
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
     * Devuelve una representación en forma de cadena de la pregunta.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Pregunta #" + idPregunta;
    }
}
