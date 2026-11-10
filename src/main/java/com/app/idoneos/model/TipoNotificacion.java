package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un tipo de notificación que el sistema envía por correo. */
@Entity
@Table(name = "TipoNotificacion")
@Getter
@Setter
@NoArgsConstructor
public class TipoNotificacion {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoNotificacion")
    private int idTipoNotificacion;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el registro con los datos básicos. */
    public TipoNotificacion(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Aviso de que una cohorte pendiente de contenido pasó a inscripción próxima. */
    public static final String COHORTE_ACTIVADA = "Cohorte activada";

    /** Aviso de que una cohorte pendiente se canceló. */
    public static final String COHORTE_CANCELADA = "Cohorte cancelada";

    /** Aviso que recibe el alumno que marcó "Recordarme" en una cohorte próxima. */
    public static final String RECORDATORIO_COHORTE = "Recordatorio de cohorte";
}
