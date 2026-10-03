package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Opción de respuesta de una pregunta, indicando si es correcta.
 */
@Entity
@Table(name = "OpcionRespuesta")
@Getter
@Setter
@NoArgsConstructor
public class OpcionRespuesta {

    /**
     * Identificador único de la opción de respuesta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOpcionRespuesta")
    private int idOpcionRespuesta;

    /**
     * Relación con Pregunta.
     */
    @ManyToOne
    @JoinColumn(name = "idPregunta", nullable = false)
    private Pregunta pregunta;

    /**
     * Texto de la opción de respuesta.
     */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /**
     * Indica si la opción es la respuesta correcta.
     */
    @Column(name = "esCorrecta", nullable = false)
    private Boolean esCorrecta = false;

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
    @OneToMany(mappedBy = "opcionRespuesta", cascade = CascadeType.ALL)
    private Set<RespuestaIntento> respuestasIntento = new HashSet<>();

    /**
     * Constructor para crear la opción de respuesta con los datos básicos.
     *
     * @param pregunta relación con Pregunta.
     * @param texto texto de la opción de respuesta.
     * @param esCorrecta indica si la opción es la respuesta correcta.
     */
    public OpcionRespuesta(Pregunta pregunta, String texto, Boolean esCorrecta) {
        this.pregunta = pregunta;
        this.texto = texto;
        this.esCorrecta = esCorrecta;
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
     * Devuelve una representación en forma de cadena de la opción de respuesta.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "OpcionRespuesta #" + idOpcionRespuesta;
    }
}
