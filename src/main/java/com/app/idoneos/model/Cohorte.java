package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una edición de un programa, con sus fechas de inscripción y dictado, cupo y duración del acceso. */
@Entity
@Table(name = "Cohorte")
@Check(name = "ck_cohorte_inscripcion", constraints = "cast(fecha_fin_inscripcion as date) > cast(fecha_inicio_inscripcion as date)")
@Check(name = "ck_cohorte_dictado", constraints = "fecha_inicio_dictado is null or fecha_fin_dictado is null or fecha_fin_dictado > fecha_inicio_dictado")
@Check(name = "ck_cohorte_inicio_dictado", constraints = "fecha_inicio_dictado is null or cast(fecha_inicio_dictado as date) > cast(fecha_fin_inscripcion as date)")
@Check(name = "ck_cohorte_cupo", constraints = "cupo_maximo is null or (cupo_maximo > 0 and cupo_maximo <= 10000)")
@Check(name = "ck_cohorte_acceso_dias", constraints = "acceso_dias > 0 and acceso_dias <= 700")
@Getter
@Setter
@NoArgsConstructor
public class Cohorte {

    /** Máximo de días de acceso al contenido desde la inscripción. */
    public static final int DIAS_ACCESO_MAXIMOS = 700;

    /** Máximo de inscriptos que se puede definir como cupo de una cohorte. */
    public static final int CUPO_MAXIMO_PERMITIDO = 10_000;

    /** Identificador único de la cohorte. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCohorte")
    private int idCohorte;

    /** Relación con Curso: el curso del que es la cohorte. */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Programa de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPrograma", nullable = true)
    private Programa programa;

    /** Relación con EstadoCohorte, el estado guardado de la cohorte. */
    @ManyToOne
    @JoinColumn(name = "idEstadoCohorte", nullable = false)
    private EstadoCohorte estadoCohorte;

    /** Fecha y hora de inicio del período de inscripción. */
    @Column(name = "fechaInicioInscripcion", nullable = false)
    private LocalDateTime fechaInicioInscripcion;

    /** Fecha y hora de fin del período de inscripción. */
    @Column(name = "fechaFinInscripcion", nullable = false)
    private LocalDateTime fechaFinInscripcion;

    /** Fecha y hora de inicio del dictado. */
    @Column(name = "fechaInicioDictado", nullable = true)
    private LocalDateTime fechaInicioDictado;

    /** Fecha y hora de fin del dictado. */
    @Column(name = "fechaFinDictado", nullable = true)
    private LocalDateTime fechaFinDictado;

    /** Cupo máximo de inscriptos. */
    @Column(name = "cupoMaximo", nullable = true)
    private Integer cupoMaximo;

    /** Observaciones del administrador sobre la cohorte. */
    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

    /** Cantidad de días de acceso al contenido luego de inscribirse. */
    @Column(name = "accesoDias", nullable = false)
    private int accesoDias;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Clases en vivo que referencian a este registro. */
    @OneToMany(mappedBy = "cohorte", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /** Inscripciones que referencian a este registro. */
    @OneToMany(mappedBy = "cohorte", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    /** Participaciones docente que referencian a este registro. */
    @OneToMany(mappedBy = "cohortePorDefecto", cascade = CascadeType.ALL)
    private Set<DocenteCurso> participacionesDocente = new HashSet<>();

    /** Crea la cohorte con los datos básicos. */
    public Cohorte(Programa programa, LocalDateTime fechaInicioInscripcion, LocalDateTime fechaFinInscripcion, int accesoDias) {
        this.programa = programa;
        this.curso = programa == null ? null : programa.getCurso();
        this.fechaInicioInscripcion = fechaInicioInscripcion;
        this.fechaFinInscripcion = fechaFinInscripcion;
        this.accesoDias = accesoDias;
    }

    /** Crea una cohorte de un curso, con o sin programa. */
    public Cohorte(Curso curso, Programa programa, LocalDateTime fechaInicioInscripcion, LocalDateTime fechaFinInscripcion,
            int accesoDias) {
        this(programa, fechaInicioInscripcion, fechaFinInscripcion, accesoDias);
        this.curso = curso;
    }

    /** Indica si la cohorte ya tiene programa. */
    public boolean tienePrograma() {
        return programa != null;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la cohorte. */
    @Override
    public String toString() {
        return "Cohorte #" + idCohorte;
    }

    /** Indica si la cohorte está pendiente. */
    public boolean esPendiente() {
        return estadoCohorte != null && EstadoCohorte.PENDIENTE.equals(estadoCohorte.getNombre());
    }

    /** Indica si la cohorte está publicada en el catálogo. */
    public boolean estaPublicada() {
        return !baja && !esPendiente() && !esCancelada();
    }

    /** Indica si la cohorte no se puede modificar: está publicada en el catálogo. */
    public boolean estaBloqueada() {
        return estaPublicada();
    }

    /** Obtiene el motivo por el que la cohorte no se puede modificar. */
    public String getMotivoDeBloqueo() {
        return "está publicada en el catálogo";
    }

    /** Indica si la cohorte está en inscripción próxima. */
    public boolean esProxima() {
        return estadoCohorte != null && EstadoCohorte.PROXIMA.equals(estadoCohorte.getNombre());
    }

    /** Indica si la cohorte fue cancelada porque venció el plazo para completar el contenido sin hacerlo. */
    public boolean esCancelada() {
        return estadoCohorte != null && EstadoCohorte.CANCELADA.equals(estadoCohorte.getNombre());
    }

    /** Indica si la cohorte tiene el contenido completo y sigue el calendario. */
    public boolean esActiva() {
        return !esPendiente() && !esCancelada();
    }

    /** Calcula en qué estado debería estar la cohorte según sus fechas y si todavía hay inscripciones en juego (sin tocar el estado guardado). */
    public String calcularEstado(LocalDateTime ahora, boolean hayInscripcionesEnJuego) {
        if (esPendiente()) {
            return EstadoCohorte.PENDIENTE;
        }
        if (esCancelada()) {
            return EstadoCohorte.CANCELADA;
        }
        if (ahora.isBefore(fechaInicioInscripcion)) {
            return EstadoCohorte.PROXIMA;
        }
        if (!ahora.isAfter(fechaFinInscripcion)) {
            return EstadoCohorte.ABIERTA;
        }
        boolean conDictado = fechaInicioDictado != null;
        if (conDictado && ahora.isBefore(fechaInicioDictado)) {
            return EstadoCohorte.CERRADA;
        }
        LocalDateTime finDictado = fechaFinDictado != null ? fechaFinDictado : fechaFinInscripcion.plusDays(accesoDias);
        if (conDictado && !ahora.isAfter(finDictado)) {
            return EstadoCohorte.EN_DICTADO;
        }
        if (!hayInscripcionesEnJuego) {
            return EstadoCohorte.CURSADA_FINALIZADA;
        }
        return conDictado ? EstadoCohorte.DICTADO_FINALIZADO : EstadoCohorte.CERRADA;
    }

    /** Calcula el estado según las fechas y las inscripciones cargadas en la cohorte. */
    public String calcularEstado(LocalDateTime ahora) {
        return calcularEstado(ahora, inscripciones.stream().anyMatch(inscripcion -> inscripcion.sigueEnJuego(ahora)));
    }

    /** Obtiene el estado de la cohorte: el guardado o, si todavía no tiene (una cohorte nueva sin guardar). */
    public String getEstado() {
        return estadoCohorte != null ? estadoCohorte.getNombre() : calcularEstado(LocalDateTime.now());
    }

    /** Obtiene el nombre con el que se reconoce la cohorte en los selectores. */
    public String getEtiqueta() {
        return getMesAnio() + " (" + getEstado() + ")";
    }

    /** Obtiene el mes y el año en que comienza la cohorte (el del dictado o, si no tiene, el de la inscripción). */
    public String getMesAnio() {
        LocalDateTime inicio = fechaInicioDictado != null ? fechaInicioDictado : fechaInicioInscripcion;
        String mes = inicio.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.of("es", "AR"));
        return mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + inicio.getYear();
    }

    /** Verifica si la cohorte tiene la inscripción abierta. */
    public boolean estaAbierta() {
        LocalDateTime ahora = LocalDateTime.now();
        return esActiva() && !ahora.isBefore(fechaInicioInscripcion) && !ahora.isAfter(fechaFinInscripcion);
    }

    /** Verifica si la inscripción a la cohorte todavía no abrió (se lee de las fechas). */
    public boolean estaProxima() {
        return esActiva() && LocalDateTime.now().isBefore(fechaInicioInscripcion);
    }

    /** Verifica si la inscripción a la cohorte ya terminó: pasó su fecha de fin de inscripción. */
    public boolean inscripcionCerrada() {
        return fechaFinInscripcion != null && LocalDateTime.now().isAfter(fechaFinInscripcion);
    }

    /** Verifica si la cohorte está en marcha: tiene el contenido completo y todavía no terminó la cursada. */
    public boolean estaActiva() {
        return !baja && esActiva() && !EstadoCohorte.CURSADA_FINALIZADA.equals(getEstado());
    }

    /** Cuenta las inscripciones vigentes (no dadas de baja) de la cohorte. */
    public int getCantidadInscriptos() {
        return (int) inscripciones.stream().filter(inscripcion -> !inscripcion.getBaja()).count();
    }

    /** Calcula el cupo disponible de la cohorte. */
    public Integer getCupoDisponible() {
        return cupoMaximo == null ? null : Math.max(0, cupoMaximo - getCuposOcupados());
    }

    /** Cuenta los cupos ocupados: las inscripciones activas que ya están habilitadas. */
    public int getCuposOcupados() {
        return (int) inscripciones.stream().filter(inscripcion -> !inscripcion.getBaja() && inscripcion.ocupaCupo())
                .count();
    }

    /** Indica si la cohorte tiene cupo para habilitar una inscripción más. */
    public boolean tieneCupo() {
        return cupoMaximo == null || getCuposOcupados() < cupoMaximo;
    }

    /** Calcula hasta cuándo tiene acceso quien se habilita en este momento. */
    public LocalDateTime calcularVencimientoAcceso(LocalDateTime fechaInscripcion) {
        LocalDateTime base = fechaInicioDictado != null && fechaInicioDictado.isAfter(fechaInscripcion)
                ? fechaInicioDictado : fechaInscripcion;
        return base.plusDays(accesoDias);
    }

    /** Indica si el dictado de la cohorte todavía no comenzó (solo las cohortes con fechas de dictado). */
    public boolean dictadoPorComenzar(LocalDateTime ahora) {
        return fechaInicioDictado != null && ahora.isBefore(fechaInicioDictado);
    }

    /** Obtiene la fecha desde la que se cuentan los días del cronograma: el inicio del dictado o, si la cohorte no tiene fechas de dictado, el inicio de la inscripción. */
    public LocalDateTime getFechaBaseCronograma() {
        return fechaInicioDictado != null ? fechaInicioDictado : fechaInicioInscripcion;
    }

    /** Calcula cuántos días transcurrieron desde la fecha base del cronograma. */
    public int getDiasTranscurridos() {
        return (int) Math.max(0, ChronoUnit.DAYS.between(getFechaBaseCronograma(), LocalDateTime.now())) + 1;
    }

    /** Obtiene la duración del cronograma de la cohorte, en días: la de su programa o, si todavía no tiene programa, la del curso. */
    private int getDuracionDelCronograma() {
        return programa != null ? programa.getDuracionTotalDias() : curso.getDuracionDias();
    }

    /** Calcula el día del cronograma en el que está la cohorte, entre el primero y el último del programa. */
    public int getDiaActual() {
        return Math.max(1, Math.min(getDiasTranscurridos(), getDuracionDelCronograma()));
    }

    /** Indica si el cronograma de la cohorte ya comenzó: llegó la fecha desde la que se cuentan sus días. */
    public boolean haComenzadoCronograma() {
        return !LocalDateTime.now().isBefore(getFechaBaseCronograma());
    }

    /** Indica si el cronograma de la cohorte ya terminó: pasó el último día del programa. */
    public boolean haFinalizadoCronograma() {
        return haComenzadoCronograma() && getDiasTranscurridos() > getDuracionDelCronograma();
    }

    /** Calcula el estado de un tema según el calendario de la cohorte (sin mirar el avance de ningún alumno). */
    public String getEstadoTemaResumido(TemaCronograma temaCronograma) {
        if (!haComenzadoCronograma()) {
            return "Pendiente";
        }
        int dia = getDiasTranscurridos();
        if (temaCronograma.getDiaHasta() < dia) {
            return "Completada";
        }
        return temaCronograma.getDiaDesde() <= dia ? "En curso" : "Pendiente";
    }

    /** Obtiene las clases en vivo de la cohorte que no están dadas de baja ni ocultas. */
    public List<ClaseEnVivo> getClasesEnVivoPublicadas() {
        return clasesEnVivo.stream().filter(clase -> !clase.getBaja() && !clase.getOculto())
                .sorted(Comparator.comparing(ClaseEnVivo::getFechaHora)).toList();
    }

    /** Obtiene la clase que se está transmitiendo en este momento. */
    public ClaseEnVivo getClaseEnCurso() {
        return getClasesEnVivoPublicadas().stream()
                .filter(clase -> EstadoClaseEnVivo.EN_VIVO.equals(clase.getEstadoClaseEnVivo().getNombre()))
                .findFirst().orElse(null);
    }
}
