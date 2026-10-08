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
 * Inscripción de un alumno a una cohorte. Define hasta cuándo tiene acceso al contenido.
 */
@Entity
@Table(name = "Inscripcion")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    /**
     * Identificador único de la inscripción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscripcion")
    private int idInscripcion;

    /**
     * Relación con Cohorte.
     */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    /**
     * Relación con Alumno.
     */
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /**
     * Fecha y hora en que vence el acceso al contenido.
     */
    @Column(name = "fechaVencimientoAcceso", nullable = false)
    private LocalDateTime fechaVencimientoAcceso;

    /**
     * Observaciones sobre la inscripción. Es opcional.
     */
    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

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
     * Indica si la inscripción está habilitada. Es opcional.
     */
    @Column(name = "habilitado", nullable = true)
    private Boolean habilitado;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Conjunto de progresos asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Progreso> progresos = new HashSet<>();

    /**
     * Conjunto de consultasForo asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<ConsultaForo> consultasForo = new HashSet<>();

    /**
     * Conjunto de pagos asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    /**
     * Conjunto de intentosAutoevaluacion asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<IntentoAutoevaluacion> intentosAutoevaluacion = new HashSet<>();

    /**
     * Conjunto de certificados asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Certificado> certificados = new HashSet<>();

    /**
     * Constructor para crear la inscripción con los datos básicos.
     *
     * @param cohorte relación con Cohorte.
     * @param alumno relación con Alumno.
     * @param fechaVencimientoAcceso fecha y hora en que vence el acceso al contenido.
     */
    public Inscripcion(Cohorte cohorte, Alumno alumno, LocalDateTime fechaVencimientoAcceso) {
        this.cohorte = cohorte;
        this.alumno = alumno;
        this.fechaVencimientoAcceso = fechaVencimientoAcceso;
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
     * Devuelve una representación en forma de cadena de la inscripción.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Inscripcion #" + idInscripcion;
    }

    /**
     * Obtiene el curso al que pertenece la inscripción, a través de su cohorte y programa.
     *
     * @return El curso de la inscripción.
     */
    public Curso getCurso() {
        return cohorte.getPrograma().getCurso();
    }

    /**
     * Cuenta las unidades del cronograma del programa que el alumno ya completó.
     *
     * @return La cantidad de unidades completadas.
     */
    public int getUnidadesCompletadas() {
        return (int) progresos.stream().filter(Progreso::getCompletada).count();
    }

    /**
     * Calcula el progreso general del alumno en el curso, según las unidades completadas.
     *
     * @return El porcentaje de progreso (0-100).
     */
    public int getPorcentajeProgreso() {
        int total = cohorte.getPrograma().getCantidadUnidades();
        return total == 0 ? 0 : Math.min(100, getUnidadesCompletadas() * 100 / total);
    }

    /**
     * Calcula el estado de la inscripción: Pendiente (sin unidades completadas), En Progreso o Finalizado.
     *
     * @return El estado de la inscripción.
     */
    public String getEstado() {
        int porcentaje = getPorcentajeProgreso();
        if (porcentaje >= 100) {
            return "Finalizado";
        }
        return getUnidadesCompletadas() == 0 ? "Pendiente" : "En Progreso";
    }

    /**
     * Indica si el alumno completó una unidad.
     *
     * @param unidad La unidad a consultar.
     * @return {@code true} si la unidad figura como completada.
     */
    public boolean estaUnidadCompletada(Unidad unidad) {
        return progresos.stream()
                .anyMatch(progreso -> progreso.getCompletada() && progreso.getUnidad().getIdUnidad() == unidad.getIdUnidad());
    }

    /**
     * Indica si una unidad está habilitada según el avance secuencial del alumno: es la primera del cronograma o
     * la unidad anterior figura como completada.
     *
     * @param unidadCronograma La unidad del cronograma a consultar.
     * @return {@code true} si el alumno puede acceder a la unidad.
     */
    public boolean estaUnidadHabilitada(UnidadCronograma unidadCronograma) {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getNumeroOrden() < unidadCronograma.getNumeroOrden())
                .max(Comparator.comparingInt(UnidadCronograma::getNumeroOrden))
                .map(anterior -> estaUnidadCompletada(anterior.getUnidad())).orElse(true);
    }

    /**
     * Obtiene la unidad que el alumno debe cursar ahora: la primera del cronograma que todavía no completó.
     *
     * @return La unidad en curso, o {@code null} si completó todas.
     */
    public UnidadCronograma getUnidadEnCurso() {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> !estaUnidadCompletada(cronograma.getUnidad())).findFirst().orElse(null);
    }

    /**
     * Calcula el estado de una unidad para el alumno: Completada, En curso (habilitada y sin completar) o Bloqueada.
     *
     * @param unidadCronograma La unidad del cronograma a consultar.
     * @return El estado de la unidad.
     */
    public String getEstadoUnidad(UnidadCronograma unidadCronograma) {
        if (estaUnidadCompletada(unidadCronograma.getUnidad())) {
            return "Completada";
        }
        return estaUnidadHabilitada(unidadCronograma) ? "En curso" : "Bloqueada";
    }

    /**
     * Obtiene el número de orden de la última unidad que el alumno completó.
     *
     * @return El número de orden, o 0 si no completó ninguna.
     */
    public int getUltimaUnidadCompletada() {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> estaUnidadCompletada(cronograma.getUnidad()))
                .mapToInt(UnidadCronograma::getNumeroOrden).max().orElse(0);
    }

    /**
     * Obtiene el número de orden de la última unidad que, según el cronograma, ya debería estar terminada hoy.
     *
     * @return El número de orden, o 0 si todavía no terminó ninguna semana de ninguna unidad.
     */
    public int getUnidadesEsperadasTerminadas() {
        Programa programa = cohorte.getPrograma();
        return programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> programa.getSemanaHasta(cronograma) < cohorte.getSemanasTranscurridas())
                .mapToInt(UnidadCronograma::getNumeroOrden).max().orElse(0);
    }

    /**
     * Indica si el alumno va por detrás de lo esperado: su última unidad completada es anterior a la última que
     * el cronograma ya daba por terminada.
     *
     * @return {@code true} si el alumno está atrasado.
     */
    public boolean estaAtrasada() {
        return getUltimaUnidadCompletada() < getUnidadesEsperadasTerminadas();
    }

    /**
     * Calcula el estado de una unidad respecto del cronograma: Completada, Atrasada (debería estar terminada),
     * Semana actual o Próxima.
     *
     * @param unidadCronograma La unidad del cronograma a consultar.
     * @return El estado de la unidad en el cronograma.
     */
    public String getEstadoCronograma(UnidadCronograma unidadCronograma) {
        if (estaUnidadCompletada(unidadCronograma.getUnidad())) {
            return "Completada";
        }
        int semana = cohorte.getSemanasTranscurridas();
        if (unidadCronograma.getSemanaHasta() < semana) {
            return "Atrasada";
        }
        return unidadCronograma.getSemanaDesde() <= semana ? "Semana actual" : "Próxima";
    }
}
