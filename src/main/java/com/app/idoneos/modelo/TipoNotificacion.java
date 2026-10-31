package com.app.idoneos.modelo;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de notificación que el sistema envía por correo.
 */
@Entity
@Table(name = "TipoNotificacion")
@Getter
@Setter
@NoArgsConstructor
public class TipoNotificacion {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoNotificacion")
    private int idTipoNotificacion;

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
    public TipoNotificacion(String nombre) {
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

    /** Aviso de que una cohorte pendiente pasó a activa. */
    public static final String COHORTE_ACTIVADA = "Cohorte activada";

    /** Aviso de que una cohorte pendiente se canceló. */
    public static final String COHORTE_CANCELADA = "Cohorte cancelada";
}
