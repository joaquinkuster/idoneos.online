package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modalidad de dictado de un curso (en vivo, grabada o con clon de inteligencia artificial).
 */
@Entity
@Table(name = "Modalidad")
@Getter
@Setter
@NoArgsConstructor
public class Modalidad {

    /**
     * Nombre de la modalidad con clases en vivo.
     */
    public static final String EN_VIVO = "En vivo";

    /**
     * Nombre de la modalidad con clases grabadas.
     */
    public static final String GRABADA = "Grabada";

    /**
     * Nombre de la modalidad con clases dictadas por un clon de inteligencia artificial.
     */
    public static final String CLON_IA = "Con clon de IA";

    /**
     * Identificador único de la modalidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idModalidad")
    private int idModalidad;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear la modalidad con los datos básicos.
     *
     * @param nombre nombre.
     */
    public Modalidad(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena de la modalidad.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
