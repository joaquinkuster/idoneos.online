package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado del ciclo de vida de una inscripción. */
@Entity
@Table(name = "EstadoInscripcion")
@Getter
@Setter
@NoArgsConstructor
public class EstadoInscripcion {

    /** Se inscribió y todavía no tiene un pago en curso ni el acceso habilitado. No ocupa cupo. */
    public static final String PENDIENTE_DE_PAGO = "Pendiente de pago";

    /** Tiene un pago online iniciado dentro del plazo de confirmación: reserva el cupo hasta que se resuelva. */
    public static final String PAGO_EN_CURSO = "Pago en curso";

    /** Pagó, es gratuita o la habilitó el administrador: tiene acceso y ocupa cupo. */
    public static final String HABILITADA = "Acceso habilitado";

    /** El administrador le suspendió el acceso, que antes estaba habilitado. */
    public static final String SUSPENDIDA = "Acceso suspendido";

    /** Llegó la fecha de vencimiento de su acceso (se calcula una sola vez, al habilitarla, y no se modifica). */
    public static final String VENCIDA = "Acceso vencido";

    /** La canceló el sistema: terminó la inscripción sin que se pagara. */
    public static final String CANCELADA = "Cancelada por falta de pago";

    /** El alumno o el administrador la dio de baja. No ocupa cupo. */
    public static final String DADA_DE_BAJA = "Dada de baja";

    /** Identificador único del estado de la inscripción. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoInscripcion")
    private int idEstadoInscripcion;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el estado de la inscripción con los datos básicos. */
    public EstadoInscripcion(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del estado de la inscripción. */
    @Override
    public String toString() {
        return nombre;
    }
}
