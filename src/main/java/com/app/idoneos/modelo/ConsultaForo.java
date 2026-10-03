package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consulta realizada por un alumno en el foro de una unidad.
 */
@Entity
@Table(name = "ConsultaForo")
@Getter
@Setter
@NoArgsConstructor
public class ConsultaForo {

    /**
     * Identificador único de la consulta del foro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConsulta")
    private int idConsulta;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Texto de la consulta.
     */
    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

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
     * Conjunto de respuestasForo asociados.
     */
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL)
    private Set<RespuestaForo> respuestasForo = new HashSet<>();

    /**
     * Constructor para crear la consulta del foro con los datos básicos.
     *
     * @param unidad relación con Unidad.
     * @param inscripcion relación con Inscripcion.
     * @param texto texto de la consulta.
     */
    public ConsultaForo(Unidad unidad, Inscripcion inscripcion, String texto) {
        this.unidad = unidad;
        this.inscripcion = inscripcion;
        this.texto = texto;
    }

    /**
     * Devuelve una representación en forma de cadena de la consulta del foro.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "ConsultaForo #" + idConsulta;
    }
}
