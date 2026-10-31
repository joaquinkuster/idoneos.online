package com.app.idoneos.modelo;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del envío de una notificación.
 */
@Entity
@Table(name = "EstadoEnvio")
@Getter
@Setter
@NoArgsConstructor
public class EstadoEnvio {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoEnvio")
    private int idEstadoEnvio;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el registro con los datos básicos.
     *
     * @param nombre nombre.
     */
    public EstadoEnvio(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }

    /** Estado de una notificación que espera su envío. */
    public static final String PENDIENTE = "Pendiente";

    /** Estado de una notificación enviada. */
    public static final String ENVIADO = "Enviado";

    /** Estado de una notificación cuyo envío falló. */
    public static final String ERROR = "Error";
}
