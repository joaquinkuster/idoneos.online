package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del avance de un alumno en su inscripción: En progreso (aunque todavía no haya completado ningún tema) o Finalizado (completó todos los
 * temas del cronograma).
 *
 * <p>Se guarda porque calcularlo obliga a contar los temas completados contra los del cronograma, una consulta por inscripción. Cambia por un
 * evento: cuando el alumno completa el último tema.</p>
 */
@Entity
@Table(name = "EstadoAvance")
@Getter
@Setter
@NoArgsConstructor
public class EstadoAvance {

    /** Todavía no completó todos los temas. */
    public static final String EN_PROGRESO = "En progreso";

    /** Completó todos los temas del cronograma. */
    public static final String FINALIZADO = "Finalizado";

    /**
     * Identificador único del estado del avance.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoAvance")
    private int idEstadoAvance;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el estado del avance con los datos básicos.
     *
     * @param nombre nombre.
     */
    public EstadoAvance(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del estado del avance.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
