package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la evaluación de un tema, armada con pools de preguntas y con tiempo límite. */
@Entity
@Table(name = "Autoevaluacion")
@Check(name = "ck_autoevaluacion_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_autoevaluacion_tiempo", constraints = "tiempo_limite_minutos is null or tiempo_limite_minutos > 0")
@Check(name = "ck_autoevaluacion_preguntas", constraints = "cantidad_preguntas > 0")
@Check(name = "ck_autoevaluacion_nota", constraints = "nota_minima > 0 and nota_minima <= 10")
@Check(name = "ck_autoevaluacion_intentos", constraints = "intentos_permitidos is null or intentos_permitidos > 0")
@Check(name = "ck_autoevaluacion_cierre", constraints = "fecha_cierre is null or fecha_cierre > fecha_apertura")
@Getter
@Setter
@NoArgsConstructor
public class Autoevaluacion {

    /** Identificador único de la autoevaluación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAutoevaluacion")
    private int idAutoevaluacion;

    /** Tema de este registro. */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Nombre de la autoevaluación. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Tiempo límite para resolverla, en minutos. */
    @Column(name = "tiempoLimiteMinutos", nullable = true)
    private Integer tiempoLimite;

    /** Cantidad de preguntas que se extraen de los pools. */
    @Column(name = "cantidadPreguntas", nullable = false)
    private int cantidadPreguntas;

    /** Nota mínima para aprobar. */
    @Column(name = "notaMinima", nullable = false)
    private float notaMinima;

    /** Cantidad de intentos permitidos. */
    @Column(name = "intentosPermitidos", nullable = true)
    private Integer intentosPermitidos;

    /** Fecha y hora desde la que está disponible. */
    @Column(name = "fechaApertura", nullable = false)
    private LocalDateTime fechaApertura;

    /** Fecha y hora hasta la que está disponible. */
    @Column(name = "fechaCierre", nullable = true)
    private LocalDateTime fechaCierre;

    /** Indica si cada opción incorrecta que elige el alumno resta lo que vale una opción correcta de esa pregunta. */
    @Column(name = "descuentoPorError", nullable = false)
    private Boolean descuentoPorError = false;

    /** Indica si el alumno puede desplazarse libremente entre las preguntas y modificar sus respuestas. */
    @Column(name = "navegacionLibre", nullable = false)
    private Boolean navegacionLibre = false;

    /** Indica si el alumno puede salir del cuestionario y retomarlo. */
    @Column(name = "permiteSalir", nullable = false)
    private Boolean permiteSalir = false;

    /** Indica si el elemento está oculto para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Autoevaluacion pools que referencian a este registro. */
    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private Set<AutoevaluacionPool> autoevaluacionPools = new HashSet<>();

    /** Intentos autoevaluacion que referencian a este registro. */
    @OneToMany(mappedBy = "autoevaluacion", cascade = CascadeType.ALL)
    private Set<IntentoAutoevaluacion> intentosAutoevaluacion = new HashSet<>();

    /** Crea la autoevaluación con los datos básicos. */
    public Autoevaluacion(Tema tema, String nombre, int cantidadPreguntas, float notaMinima, LocalDateTime fechaApertura) {
        this.tema = tema;
        this.nombre = nombre;
        this.cantidadPreguntas = cantidadPreguntas;
        this.notaMinima = notaMinima;
        this.fechaApertura = fechaApertura;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la autoevaluación. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Indica si la autoevaluación está vigente y los alumnos la ven (no está dada de baja ni oculta). */
    public boolean esVisible() {
        return !baja && !oculto;
    }

    /** Calcula cuántos puntos (sobre 10) vale cada pregunta del cuestionario. */
    public double getValorDeCadaPregunta() {
        return cantidadPreguntas <= 0 ? 0 : 10d / cantidadPreguntas;
    }

    /** Indica si la autoevaluación combina pools de otros temas (anteriores a la suya). */
    public boolean esEvaluacionFinal() {
        return getPoolsActivos().stream().anyMatch(pool -> pool.getTema().getIdTema() != tema.getIdTema());
    }

    /** Indica si la fecha dada está dentro del período habilitado: desde la fecha de apertura y, si tiene. */
    public boolean estaAbierta(LocalDateTime momento) {
        return !momento.isBefore(fechaApertura) && (fechaCierre == null || !momento.isAfter(fechaCierre));
    }

    /** Obtiene los pools vigentes asociados a la autoevaluación. */
    public List<Pool> getPoolsActivos() {
        return autoevaluacionPools.stream().map(AutoevaluacionPool::getPool).filter(pool -> !pool.getBaja()).distinct()
                .sorted(Comparator.comparingInt(Pool::getIdPool)).toList();
    }

    /** Obtiene las preguntas vigentes de los pools asociados, de las que se sortea cada intento. */
    public List<Pregunta> getPreguntasDisponibles() {
        return getPoolsActivos().stream().flatMap(pool -> pool.getPreguntasActivas().stream()).toList();
    }

    /** Indica si los pools asociados reúnen, en conjunto. */
    public boolean tienePreguntasSuficientes() {
        return getPreguntasDisponibles().size() >= cantidadPreguntas;
    }

    /** Obtiene los intentos vigentes de todos los alumnos sobre la autoevaluación. */
    public List<IntentoAutoevaluacion> getIntentosActivos() {
        return intentosAutoevaluacion.stream().filter(intento -> !intento.getBaja())
                .sorted(Comparator.comparingInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion)).toList();
    }

    /** Indica si algún alumno registra intentos vigentes sobre la autoevaluación. */
    public boolean tieneIntentosActivos() {
        return intentosAutoevaluacion.stream().anyMatch(intento -> !intento.getBaja());
    }

    /** Obtiene el intento que un alumno tiene en curso sobre la autoevaluación. */
    public IntentoAutoevaluacion getIntentoEnCurso(Inscripcion inscripcion) {
        return intentosDe(inscripcion).stream().filter(IntentoAutoevaluacion::estaEnCurso).findFirst().orElse(null);
    }

    /** Obtiene los intentos vigentes que un alumno registró sobre la autoevaluación. */
    public List<IntentoAutoevaluacion> intentosDe(Inscripcion inscripcion) {
        return intentosAutoevaluacion.stream()
                .filter(intento -> !intento.getBaja()
                        && intento.getInscripcion().getIdInscripcion() == inscripcion.getIdInscripcion())
                .sorted(Comparator.comparingInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion)).toList();
    }

    /** Calcula cuántos intentos le quedan a un alumno. */
    public Integer intentosRestantes(Inscripcion inscripcion) {
        return intentosPermitidos == null ? null : Math.max(0, intentosPermitidos - intentosDe(inscripcion).size());
    }

    /** Obtiene la mejor nota que un alumno obtuvo entre sus intentos entregados. */
    public Float mejorNota(Inscripcion inscripcion) {
        return intentosDe(inscripcion).stream().map(IntentoAutoevaluacion::getNota).filter(nota -> nota != null)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    /** Indica si un alumno aprobó la autoevaluación en alguno de sus intentos (automática o manualmente). */
    public boolean estaAprobadaPor(Inscripcion inscripcion) {
        return intentosDe(inscripcion).stream().anyMatch(intento -> Boolean.TRUE.equals(intento.getAprobado()));
    }
}
