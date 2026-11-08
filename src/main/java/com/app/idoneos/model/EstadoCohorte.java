package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado en el que se encuentra una cohorte, siempre guardado: Pendiente de contenido (todavía no tiene el contenido completo), Inscripción
 * próxima, Inscripción abierta, Inscripción cerrada, En dictado, Dictado finalizado, Cursada finalizada (ya no queda nadie cursando) o
 * Cancelada por falta de contenido (venció el plazo para completarlo). Una tarea periódica la mueve de estado cuando llega cada fecha.
 */
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

    /** Cerró la inscripción y el dictado todavía no comenzó (o la cohorte no tiene dictado, solo acceso a los contenidos). */
    public static final String CERRADA = "Inscripción cerrada";

    /** El dictado está en marcha (solo las cohortes con fechas de dictado). */
    public static final String EN_DICTADO = "En dictado";

    /** Terminó el dictado, pero todavía hay alumnos con acceso o pagos pendientes (solo las cohortes con fechas de dictado). */
    public static final String DICTADO_FINALIZADO = "Dictado finalizado";

    /** Terminó todo: cerró la inscripción, terminó el dictado y ninguna inscripción sigue en juego. */
    public static final String CURSADA_FINALIZADA = "Cursada finalizada";

    /** Venció el plazo para completar el contenido sin hacerlo. */
    public static final String CANCELADA = "Cancelada por falta de contenido";

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
