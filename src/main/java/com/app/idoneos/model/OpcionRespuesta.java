package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una opción de respuesta de una pregunta, que indica si es correcta. */
@Entity
@Table(name = "OpcionRespuesta")
@Check(name = "ck_opcion_respuesta_texto", constraints = "trim(texto) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class OpcionRespuesta {

    /** Identificador único de la opción de respuesta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOpcionRespuesta")
    private int idOpcionRespuesta;

    /** Pregunta de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPregunta", nullable = false)
    private Pregunta pregunta;

    /** Texto de la opción de respuesta. */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /** Indica si la opción es la respuesta correcta. */
    @Column(name = "esCorrecta", nullable = false)
    private Boolean esCorrecta = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Respuestas intento que referencian a este registro. */
    @OneToMany(mappedBy = "opcionRespuesta", cascade = CascadeType.ALL)
    private Set<RespuestaIntento> respuestasIntento = new HashSet<>();

    /** Crea la opción de respuesta con los datos básicos. */
    public OpcionRespuesta(Pregunta pregunta, String texto, Boolean esCorrecta) {
        this.pregunta = pregunta;
        this.texto = texto;
        this.esCorrecta = esCorrecta;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Calcula cuánto suma o resta a la nota del intento esta opción cuando el alumno la elige. */
    public double calcularValorOpcion(boolean descuentoPorError, int cantidadPreguntas) {
        return pregunta.calcularValorOpcion(esCorrecta, descuentoPorError, cantidadPreguntas);
    }

    /** Devuelve una representación en forma de cadena de la opción de respuesta. */
    @Override
    public String toString() {
        return "OpcionRespuesta #" + idOpcionRespuesta;
    }
}
