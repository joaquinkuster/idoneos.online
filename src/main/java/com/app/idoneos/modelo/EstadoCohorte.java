package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado en el que se encuentra una cohorte: Pendiente (todavía no tiene el contenido completo), Activa (el contenido
 * está completo y sigue el calendario de inscripción y dictado) o Cancelada (venció el plazo para completar el contenido).
 */
@Entity
@Table(name = "EstadoCohorte")
@Getter
@Setter
@NoArgsConstructor
public class EstadoCohorte {

    /** Estados de una cohorte. */
    public static final String PENDIENTE = "Pendiente";
    public static final String ACTIVA = "Activa";
    public static final String CANCELADA = "Cancelada";

    /**
     * Identificador único del estado de la cohorte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoCohorte")
    private int idEstadoCohorte;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el estado de la cohorte con los datos básicos.
     *
     * @param nombre nombre.
     */
    public EstadoCohorte(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del estado de la cohorte.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
