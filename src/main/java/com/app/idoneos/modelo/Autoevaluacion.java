package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evaluación de una unidad, armada a partir de pools de preguntas, con tiempo límite, nota mínima e intentos permitidos.
 */
@Entity
@Table(name = "Autoevaluacion")
@Getter
@Setter
@NoArgsConstructor
public class Autoevaluacion {

    /**
     * Identificador único de la autoevaluación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAutoevaluacion")
    private int idAutoevaluacion;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Nombre de la autoevaluación.
     */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /**
     * Tiempo límite para resolverla, en minutos. Es opcional.
     */
    @Column(name = "tiempoLimite", nullable = true)
    private Integer tiempoLimite;

    /**
     * Cantidad de preguntas que se extraen de los pools.
     */
    @Column(name = "cantidadPreguntas", nullable = false)
    private int cantidadPreguntas;

    /**
     * Nota mínima para aprobar.
     */
    @Column(name = "notaMinima", nullable = false)
    private float notaMinima;

    /**
     * Cantidad de intentos permitidos. Es opcional.
     */
    @Column(name = "intentosPermitidos", nullable = true)
    private Integer intentosPermitidos;

    /**
     * Fecha y hora desde la que está disponible.
     */
    @Column(name = "fechaApertura", nullable = false)
    private LocalDateTime fechaApertura;

    /**
     * Fecha y hora hasta la que está disponible. Es opcional.
     */
    @Column(name = "fechaCierre", nullable = true)
    private LocalDateTime fechaCierre;

    /**
     * Puntaje que se descuenta por cada respuesta incorrecta.
     */
    @Column(name = "descuentoPorError", nullable = false)
    private Boolean descuentoPorError = false;

    /**
     * Indica si el alumno puede volver a preguntas anteriores.
     */
    @Column(name = "permiteRetroceder", nullable = false)
    private Boolean permiteRetroceder = false;

    /**
     * Indica si el alumno puede salir y retomar el intento.
     */
    @Column(name = "permiteSalir", nullable = false)
    private Boolean permiteSalir = false;

    /**
     * Indica si el elemento está oculto para los alumnos.
     */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

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
     * Conjunto de autoevaluacionPools asociados.
     */
    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private Set<AutoevaluacionPool> autoevaluacionPools = new HashSet<>();

    /**
     * Conjunto de intentosAutoevaluacion asociados.
     */
    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private Set<IntentoAutoevaluacion> intentosAutoevaluacion = new HashSet<>();

    /**
     * Constructor para crear la autoevaluación con los datos básicos.
     *
     * @param unidad relación con Unidad.
     * @param nombre nombre de la autoevaluación.
     * @param cantidadPreguntas cantidad de preguntas que se extraen de los pools.
     * @param notaMinima nota mínima para aprobar.
     * @param fechaApertura fecha y hora desde la que está disponible.
     */
    public Autoevaluacion(Unidad unidad, String nombre, int cantidadPreguntas, float notaMinima, LocalDateTime fechaApertura) {
        this.unidad = unidad;
        this.nombre = nombre;
        this.cantidadPreguntas = cantidadPreguntas;
        this.notaMinima = notaMinima;
        this.fechaApertura = fechaApertura;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena de la autoevaluación.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene los intentos vigentes que un alumno registró sobre la autoevaluación.
     *
     * @param inscripcion La inscripción del alumno.
     * @return Los intentos, del más antiguo al más reciente.
     */
    public List<IntentoAutoevaluacion> intentosDe(Inscripcion inscripcion) {
        return intentosAutoevaluacion.stream()
                .filter(intento -> !intento.getBaja()
                        && intento.getInscripcion().getIdInscripcion() == inscripcion.getIdInscripcion())
                .sorted(Comparator.comparingInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion)).toList();
    }

    /**
     * Calcula cuántos intentos le quedan a un alumno.
     *
     * @param inscripcion La inscripción del alumno.
     * @return Los intentos restantes, o {@code null} si la autoevaluación no limita los intentos.
     */
    public Integer intentosRestantes(Inscripcion inscripcion) {
        return intentosPermitidos == null ? null : Math.max(0, intentosPermitidos - intentosDe(inscripcion).size());
    }

    /**
     * Obtiene la mejor nota que un alumno obtuvo entre sus intentos entregados.
     *
     * @param inscripcion La inscripción del alumno.
     * @return La mejor nota, o {@code null} si todavía no entregó ningún intento.
     */
    public Float mejorNota(Inscripcion inscripcion) {
        return intentosDe(inscripcion).stream().map(IntentoAutoevaluacion::getNota).filter(nota -> nota != null)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Indica si un alumno aprobó la autoevaluación en alguno de sus intentos.
     *
     * @param inscripcion La inscripción del alumno.
     * @return {@code true} si algún intento está aprobado.
     */
    public boolean estaAprobadaPor(Inscripcion inscripcion) {
        return intentosDe(inscripcion).stream().anyMatch(intento -> Boolean.TRUE.equals(intento.getAprobado()));
    }
}
