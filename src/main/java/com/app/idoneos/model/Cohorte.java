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

/**
 * Edición de un programa con sus fechas de inscripción y dictado, cupo y duración del acceso.
 */
@Entity
@Table(name = "Cohorte")
@Check(name = "ck_cohorte_inscripcion", constraints = "fecha_fin_inscripcion > fecha_inicio_inscripcion")
@Check(name = "ck_cohorte_dictado", constraints = "fecha_inicio_dictado is null or fecha_fin_dictado is null or fecha_fin_dictado > fecha_inicio_dictado")
@Check(name = "ck_cohorte_inicio_dictado", constraints = "fecha_inicio_dictado is null or fecha_inicio_dictado >= fecha_fin_inscripcion")
@Check(name = "ck_cohorte_cupo", constraints = "cupo_maximo is null or cupo_maximo > 0")
@Check(name = "ck_cohorte_acceso_dias", constraints = "acceso_dias > 0")
@Getter
@Setter
@NoArgsConstructor
public class Cohorte {

    /**
     * Máximo de días de acceso al contenido desde la inscripción.
     */
    public static final int DIAS_ACCESO_MAXIMOS = 700;

    /**
     * Máximo de inscriptos que se puede definir como cupo de una cohorte.
     */
    public static final int CUPO_MAXIMO_PERMITIDO = 10_000;

    /**
     * Identificador único de la cohorte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCohorte")
    private int idCohorte;

    /**
     * Relación con Curso: el curso del que es la cohorte. El administrador puede dar de alta la cohorte antes de que el
     * docente cargue un programa.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Relación con Programa. Es opcional: una cohorte pendiente puede no tener programa hasta que el docente lo elija.
     */
    @ManyToOne
    @JoinColumn(name = "idPrograma", nullable = true)
    private Programa programa;

    /**
     * Relación con EstadoCohorte, el estado guardado de la cohorte. Una cohorte sin estado (por ejemplo, una que todavía no se guardó) se considera
     * activa y su estado se calcula por las fechas.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoCohorte", nullable = false)
    private EstadoCohorte estadoCohorte;

    /**
     * Fecha y hora de inicio del período de inscripción.
     */
    @Column(name = "fechaInicioInscripcion", nullable = false)
    private LocalDateTime fechaInicioInscripcion;

    /**
     * Fecha y hora de fin del período de inscripción.
     */
    @Column(name = "fechaFinInscripcion", nullable = false)
    private LocalDateTime fechaFinInscripcion;

    /**
     * Fecha y hora de inicio del dictado. Es opcional.
     */
    @Column(name = "fechaInicioDictado", nullable = true)
    private LocalDateTime fechaInicioDictado;

    /**
     * Fecha y hora de fin del dictado. Es opcional.
     */
    @Column(name = "fechaFinDictado", nullable = true)
    private LocalDateTime fechaFinDictado;

    /**
     * Cupo máximo de inscriptos. Es opcional.
     */
    @Column(name = "cupoMaximo", nullable = true)
    private Integer cupoMaximo;

    /**
     * Observaciones del administrador sobre la cohorte. Es opcional.
     */
    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

    /**
     * Cantidad de días de acceso al contenido luego de inscribirse.
     */
    @Column(name = "accesoDias", nullable = false)
    private int accesoDias;

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
     * Conjunto de clasesEnVivo asociados.
     */
    @OneToMany(mappedBy = "cohorte", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /**
     * Conjunto de inscripciones asociados.
     */
    @OneToMany(mappedBy = "cohorte", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "cohortePorDefecto", cascade = CascadeType.ALL)
    private Set<DocenteCurso> participacionesDocente = new HashSet<>();

    /**
     * Constructor para crear la cohorte con los datos básicos.
     *
     * @param programa relación con Programa.
     * @param fechaInicioInscripcion fecha y hora de inicio del período de inscripción.
     * @param fechaFinInscripcion fecha y hora de fin del período de inscripción.
     * @param accesoDias cantidad de días de acceso al contenido luego de inscribirse.
     */
    public Cohorte(Programa programa, LocalDateTime fechaInicioInscripcion, LocalDateTime fechaFinInscripcion, int accesoDias) {
        this.programa = programa;
        this.curso = programa == null ? null : programa.getCurso();
        this.fechaInicioInscripcion = fechaInicioInscripcion;
        this.fechaFinInscripcion = fechaFinInscripcion;
        this.accesoDias = accesoDias;
    }

    /**
     * Constructor para crear una cohorte de un curso, con o sin programa.
     *
     * @param curso relación con Curso.
     * @param programa relación con Programa (puede ser {@code null}).
     * @param fechaInicioInscripcion fecha y hora de inicio del período de inscripción.
     * @param fechaFinInscripcion fecha y hora de fin del período de inscripción.
     * @param accesoDias cantidad de días de acceso al contenido luego de inscribirse.
     */
    public Cohorte(Curso curso, Programa programa, LocalDateTime fechaInicioInscripcion, LocalDateTime fechaFinInscripcion,
            int accesoDias) {
        this(programa, fechaInicioInscripcion, fechaFinInscripcion, accesoDias);
        this.curso = curso;
    }

    /**
     * Indica si la cohorte ya tiene programa.
     *
     * @return {@code true} si el docente (o el administrador) ya le asignó un programa.
     */
    public boolean tienePrograma() {
        return programa != null;
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
     * Devuelve una representación en forma de cadena de la cohorte.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Cohorte #" + idCohorte;
    }

    /**
     * Indica si la cohorte está pendiente: el contenido del programa todavía no está completo y el docente tiene hasta el
     * inicio de la inscripción para completarlo.
     *
     * @return {@code true} si el estado de la cohorte es Pendiente.
     */
    public boolean esPendiente() {
        return estadoCohorte != null && EstadoCohorte.PENDIENTE.equals(estadoCohorte.getNombre());
    }

    /**
     * Indica si la cohorte está publicada en el catálogo: desde que entra en inscripción próxima los alumnos la ven y pueden inscribirse
     * (no cuentan las pendientes de contenido ni las canceladas). Una cohorte publicada es un compromiso con los alumnos y no se modifica.
     *
     * @return {@code true} si la cohorte está publicada en el catálogo.
     */
    public boolean estaPublicada() {
        return !baja && !esPendiente() && !esCancelada();
    }

    /**
     * Indica si la cohorte no se puede modificar: está publicada en el catálogo.
     *
     * @return {@code true} si hay un motivo que impide modificarla.
     */
    public boolean estaBloqueada() {
        return estaPublicada();
    }

    /**
     * Por qué la cohorte no se puede modificar, sin punto final (para completar «No se puede modificar la cohorte «X»: …»).
     *
     * @return El motivo.
     */
    public String getMotivoDeBloqueo() {
        return "está publicada en el catálogo";
    }

    /**
     * Indica si la cohorte está en inscripción próxima: tiene su programa completo y la inscripción todavía no abrió.
     *
     * @return {@code true} si el estado de la cohorte es Inscripción próxima.
     */
    public boolean esProxima() {
        return estadoCohorte != null && EstadoCohorte.PROXIMA.equals(estadoCohorte.getNombre());
    }

    /**
     * Indica si la cohorte fue cancelada porque venció el plazo para completar el contenido sin hacerlo.
     *
     * @return {@code true} si el estado de la cohorte es Cancelada.
     */
    public boolean esCancelada() {
        return estadoCohorte != null && EstadoCohorte.CANCELADA.equals(estadoCohorte.getNombre());
    }

    /**
     * Indica si la cohorte tiene el contenido completo y sigue el calendario: no está pendiente de contenido ni cancelada.
     *
     * @return {@code true} si no es Pendiente de contenido ni Cancelada (o todavía no tiene estado).
     */
    public boolean esActiva() {
        return !esPendiente() && !esCancelada();
    }

    /**
     * Calcula en qué estado debería estar la cohorte según sus fechas y si todavía hay inscripciones en juego (sin tocar el estado guardado).
     * Una cohorte pendiente o cancelada no sigue el calendario: conserva ese estado.
     * <ul>
     * <li>Antes de la inscripción: Inscripción próxima. Durante la inscripción: Inscripción abierta (manda aunque el dictado ya haya empezado).</li>
     * <li>Con dictado: Inscripción cerrada hasta que empieza, En dictado mientras dura y Dictado finalizado después.</li>
     * <li>Sin dictado: Inscripción cerrada.</li>
     * <li>Terminado el dictado (o sin dictado) y sin ninguna inscripción en juego: Cursada finalizada.</li>
     * </ul>
     *
     * @param ahora                    El momento de referencia.
     * @param hayInscripcionesEnJuego  Si queda alguna inscripción pendiente de pago, con un pago en curso o con el acceso por vencer.
     * @return El nombre del estado.
     */
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

    /**
     * Calcula el estado según las fechas y las inscripciones cargadas en la cohorte (ver {@link #calcularEstado(LocalDateTime, boolean)}).
     *
     * @param ahora El momento de referencia.
     * @return El nombre del estado.
     */
    public String calcularEstado(LocalDateTime ahora) {
        return calcularEstado(ahora, inscripciones.stream().anyMatch(inscripcion -> inscripcion.sigueEnJuego(ahora)));
    }

    /**
     * Obtiene el estado de la cohorte: el guardado o, si todavía no tiene (una cohorte nueva sin guardar), el que resulta de sus fechas.
     *
     * @return El nombre del estado.
     */
    public String getEstado() {
        return estadoCohorte != null ? estadoCohorte.getNombre() : calcularEstado(LocalDateTime.now());
    }

    /**
     * Obtiene el nombre con el que se reconoce la cohorte en los selectores: el mes y el año en que comienza (el del
     * dictado o, si no tiene, el de la inscripción) y su estado.
     *
     * @return Por ejemplo "Marzo 2026 (En dictado)" o "Marzo 2026 (Inscripción abierta)".
     */
    public String getEtiqueta() {
        return getMesAnio() + " (" + getEstado() + ")";
    }

    /**
     * Obtiene el mes y el año en que comienza la cohorte (el del dictado o, si no tiene, el de la inscripción).
     *
     * @return Por ejemplo "Marzo 2026".
     */
    public String getMesAnio() {
        LocalDateTime inicio = fechaInicioDictado != null ? fechaInicioDictado : fechaInicioInscripcion;
        String mes = inicio.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.of("es", "AR"));
        return mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + inicio.getYear();
    }

    /**
     * Verifica si la cohorte tiene la inscripción abierta (se lee de las fechas, no del estado guardado, que puede ir un minuto atrasado).
     *
     * @return {@code true} si la inscripción está abierta.
     */
    public boolean estaAbierta() {
        LocalDateTime ahora = LocalDateTime.now();
        return esActiva() && !ahora.isBefore(fechaInicioInscripcion) && !ahora.isAfter(fechaFinInscripcion);
    }

    /**
     * Verifica si la inscripción a la cohorte todavía no abrió (se lee de las fechas).
     *
     * @return {@code true} si la cohorte está activa y la inscripción abre más adelante.
     */
    public boolean estaProxima() {
        return esActiva() && LocalDateTime.now().isBefore(fechaInicioInscripcion);
    }

    /**
     * Verifica si la inscripción a la cohorte ya terminó: pasó su fecha de fin de inscripción. Desde ese momento no se puede iniciar ni registrar
     * un pago nuevo, y las inscripciones que siguen pendientes de pago se cancelan.
     *
     * @return {@code true} si ya pasó el fin de la inscripción.
     */
    public boolean inscripcionCerrada() {
        return fechaFinInscripcion != null && LocalDateTime.now().isAfter(fechaFinInscripcion);
    }

    /**
     * Verifica si la cohorte está en marcha: tiene el contenido completo y todavía no terminó la cursada. Los alumnos de una cohorte así cursan
     * (o van a cursar) los temas del programa. Una cohorte pendiente, cancelada o con la cursada finalizada no cuenta: todavía no tiene alumnos ni
     * los va a tener, o ya terminó.
     *
     * @return {@code true} si la cohorte está en marcha.
     */
    public boolean estaActiva() {
        return !baja && esActiva() && !EstadoCohorte.CURSADA_FINALIZADA.equals(getEstado());
    }

    /**
     * Cuenta las inscripciones vigentes (no dadas de baja) de la cohorte.
     *
     * @return La cantidad de inscriptos.
     */
    public int getCantidadInscriptos() {
        return (int) inscripciones.stream().filter(inscripcion -> !inscripcion.getBaja()).count();
    }

    /**
     * Calcula el cupo disponible de la cohorte.
     *
     * @return El cupo disponible, o {@code null} si la cohorte no tiene cupo máximo.
     */
    public Integer getCupoDisponible() {
        return cupoMaximo == null ? null : Math.max(0, cupoMaximo - getCuposOcupados());
    }

    /**
     * Cuenta los cupos ocupados: las inscripciones activas que ya están habilitadas (pagas, sin costo o dadas de alta por el administrador), las
     * suspendidas (conservan su lugar) y las que tienen un pago online en curso, que reservan el lugar hasta que el pago se resuelva. Una
     * inscripción pendiente de pago sin pago en curso, o cancelada, no ocupa cupo.
     *
     * @return La cantidad de cupos ocupados.
     */
    public int getCuposOcupados() {
        return (int) inscripciones.stream().filter(inscripcion -> !inscripcion.getBaja() && inscripcion.ocupaCupo())
                .count();
    }

    /**
     * Indica si la cohorte tiene cupo para habilitar una inscripción más.
     *
     * @return {@code true} si no tiene cupo máximo o todavía no lo alcanzó.
     */
    public boolean tieneCupo() {
        return cupoMaximo == null || getCuposOcupados() < cupoMaximo;
    }

    /**
     * Calcula hasta cuándo tiene acceso quien se habilita en este momento (al aprobarse su pago, al habilitarse sin
     * costo o al darlo de alta el administrador). Los días de acceso incluyen la duración del curso y el tiempo extra para
     * quien se atrase. Si la cohorte no tiene fechas de dictado (curso grabado), se cuentan desde el momento de la
     * habilitación. Si las tiene (curso en vivo), se cuentan desde el inicio del dictado, para que todos los alumnos
     * tengan el mismo tiempo para terminar el curso; si alguien se habilita cuando el dictado ya empezó (por ejemplo, paga
     * tarde su inscripción), se cuentan desde su habilitación.
     *
     * @param fechaInscripcion El momento de la habilitación.
     * @return La fecha de vencimiento del acceso.
     */
    public LocalDateTime calcularVencimientoAcceso(LocalDateTime fechaInscripcion) {
        LocalDateTime base = fechaInicioDictado != null && fechaInicioDictado.isAfter(fechaInscripcion)
                ? fechaInicioDictado : fechaInscripcion;
        return base.plusDays(accesoDias);
    }

    /**
     * Indica si el dictado de la cohorte todavía no comenzó (solo las cohortes con fechas de dictado).
     *
     * @param ahora El momento de referencia.
     * @return {@code true} si tiene fecha de inicio de dictado y es posterior a ese momento.
     */
    public boolean dictadoPorComenzar(LocalDateTime ahora) {
        return fechaInicioDictado != null && ahora.isBefore(fechaInicioDictado);
    }

    /**
     * Obtiene la fecha desde la que se cuentan los días del cronograma: el inicio del dictado o, si la
     * cohorte no tiene fechas de dictado, el inicio de la inscripción.
     *
     * @return La fecha base del cronograma.
     */
    public LocalDateTime getFechaBaseCronograma() {
        return fechaInicioDictado != null ? fechaInicioDictado : fechaInicioInscripcion;
    }

    /**
     * Calcula cuántos días transcurrieron desde la fecha base del cronograma, contando el día en curso
     * (el primer día es el 1). No se acota a la duración del programa.
     *
     * @return El número de día en que está la cohorte.
     */
    public int getDiasTranscurridos() {
        return (int) Math.max(0, ChronoUnit.DAYS.between(getFechaBaseCronograma(), LocalDateTime.now())) + 1;
    }

    /**
     * Duración del cronograma de la cohorte, en días: la de su programa o, si todavía no tiene programa (cohorte pendiente de contenido),
     * la del curso.
     *
     * @return La duración en días.
     */
    private int getDuracionDelCronograma() {
        return programa != null ? programa.getDuracionTotalDias() : curso.getDuracionDias();
    }

    /**
     * Calcula el día del cronograma en el que está la cohorte, entre el primero y el último del programa.
     *
     * @return El día actual del cronograma.
     */
    public int getDiaActual() {
        return Math.max(1, Math.min(getDiasTranscurridos(), getDuracionDelCronograma()));
    }

    /**
     * Indica si el cronograma de la cohorte ya comenzó: llegó la fecha desde la que se cuentan sus días.
     *
     * @return {@code true} si la fecha base del cronograma ya pasó.
     */
    public boolean haComenzadoCronograma() {
        return !LocalDateTime.now().isBefore(getFechaBaseCronograma());
    }

    /**
     * Indica si el cronograma de la cohorte ya terminó: pasó el último día del programa.
     *
     * @return {@code true} si el cronograma comenzó y ya transcurrieron todos sus días.
     */
    public boolean haFinalizadoCronograma() {
        return haComenzadoCronograma() && getDiasTranscurridos() > getDuracionDelCronograma();
    }

    /**
     * Calcula el estado de un tema según el calendario de la cohorte (sin mirar el avance de ningún alumno):
     * Completada si su último día ya pasó, En curso si el día actual está dentro de sus días y Pendiente en
     * los demás casos (incluido el cronograma que todavía no comenzó).
     *
     * @param temaCronograma El tema del cronograma a consultar.
     * @return "Completada", "En curso" o "Pendiente".
     */
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

    /**
     * Obtiene las clases en vivo de la cohorte que no están dadas de baja ni ocultas, de la más próxima a la
     * más lejana.
     *
     * @return Las clases en vivo publicadas.
     */
    public List<ClaseEnVivo> getClasesEnVivoPublicadas() {
        return clasesEnVivo.stream().filter(clase -> !clase.getBaja() && !clase.getOculto())
                .sorted(Comparator.comparing(ClaseEnVivo::getFechaHora)).toList();
    }

    /**
     * Obtiene la clase que se está transmitiendo en este momento.
     *
     * @return La clase en vivo en curso, o {@code null} si no hay ninguna.
     */
    public ClaseEnVivo getClaseEnCurso() {
        return getClasesEnVivoPublicadas().stream()
                .filter(clase -> EstadoClaseEnVivo.EN_VIVO.equals(clase.getEstadoClaseEnVivo().getNombre()))
                .findFirst().orElse(null);
    }
}
