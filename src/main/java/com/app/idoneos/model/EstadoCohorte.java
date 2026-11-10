package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado de una cohorte, que siempre se guarda. */
@Entity
@Table(name = "EstadoCohorte")
@Getter
@Setter
@NoArgsConstructor
public class EstadoCohorte {

    /** Todavía no tiene el contenido completo: el docente tiene hasta el inicio de la inscripción para completarlo. */
    public static final String PENDIENTE = "Pendiente de contenido";

    /** Tiene el contenido completo y la inscripción todavía no abrió. */
    public static final String PROXIMA = "Inscripción próxima";

    /** Se puede inscribir. */
    public static final String ABIERTA = "Inscripción abierta";

    /** Cerró la inscripción y el dictado todavía no comenzó. */
    public static final String CERRADA = "Inscripción cerrada";

    /** El dictado está en marcha (solo las cohortes con fechas de dictado). */
    public static final String EN_DICTADO = "En dictado";

    /** Terminó el dictado, pero todavía hay alumnos con acceso o pagos pendientes (solo las cohortes con fechas de dictado). */
    public static final String DICTADO_FINALIZADO = "Dictado finalizado";

    /** Terminó todo: cerró la inscripción, terminó el dictado y ninguna inscripción sigue en juego. */
    public static final String CURSADA_FINALIZADA = "Cursada finalizada";

    /** Venció el plazo para completar el contenido sin hacerlo. */
    public static final String CANCELADA = "Cancelada por falta de contenido";

    /** Identificador único del estado de la cohorte. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoCohorte")
    private int idEstadoCohorte;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el estado de la cohorte con los datos básicos. */
    public EstadoCohorte(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del estado de la cohorte. */
    @Override
    public String toString() {
        return nombre;
    }
}
