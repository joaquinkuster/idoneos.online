package com.app.idoneos.model;

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
     * Fecha y hora en que vence el acceso al contenido. Se calcula al habilitarse la inscripción por primera vez, por lo
     * que es nula mientras está pendiente de pago.
     */
    @Column(name = "fechaVencimientoAcceso", nullable = true)
    private LocalDateTime fechaVencimientoAcceso;

    /**
     * Fecha y hora en que el alumno aceptó los términos y condiciones al inscribirse.
     */
    @Column(name = "fechaAceptacionTyc", nullable = false)
    private LocalDateTime fechaAceptacionTyc = LocalDateTime.now();

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
     * Estado del ciclo de vida de la inscripción: Pendiente de pago, Pago en curso, Habilitada, Suspendida o Cancelada. Reemplaza al antiguo
     * booleano «habilitado».
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoInscripcion", nullable = false)
    private EstadoInscripcion estadoInscripcion;

    /**
     * Estado del avance del alumno: En progreso o Finalizado (completó todos los temas del cronograma). Se guarda para no tener que contar los
     * temas completados cada vez que se lo consulta.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoAvance", nullable = false)
    private EstadoAvance estadoAvance;

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
     * @param fechaVencimientoAcceso fecha y hora en que vence el acceso al contenido (nula si todavía no está habilitada: se calcula cuando se
     *                               habilita por primera vez).
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

    public static final String AVANCE_SIN_AVANCE = "Sin avance";

    private boolean esEstado(String nombre) {
        return estadoInscripcion != null && nombre.equals(estadoInscripcion.getNombre());
    }

    /**
     * Indica si la inscripción ya fue habilitada alguna vez: pagó, es gratuita o la habilitó el administrador. Incluye la suspendida y la vencida,
     * que conservan su pago y su cupo (ya no se puede decir lo mismo de «Acceso habilitado», que es el estado de quien puede entrar hoy). Una
     * inscripción sin estado (todavía no guardada) se considera habilitada.
     *
     * @return {@code true} si el estado es Acceso habilitado, suspendido o vencido.
     */
    public boolean fueHabilitada() {
        return estadoInscripcion == null || esEstado(EstadoInscripcion.HABILITADA) || esEstado(EstadoInscripcion.SUSPENDIDA)
                || esEstado(EstadoInscripcion.VENCIDA);
    }

    /**
     * Indica si el alumno tiene acceso al contenido en un momento: tiene el acceso habilitado y su fecha de vencimiento no pasó (se lee la fecha,
     * no solo el estado, porque la tarea que vence los accesos corre una vez por minuto).
     *
     * @param ahora El momento de referencia.
     * @return {@code true} si puede acceder.
     */
    public boolean tieneAcceso(LocalDateTime ahora) {
        return (estadoInscripcion == null || esEstado(EstadoInscripcion.HABILITADA)) && !vencimientoPasado(ahora);
    }

    /**
     * Indica si el alumno tiene acceso al contenido en este momento.
     *
     * @return {@code true} si puede acceder.
     */
    public boolean tieneAcceso() {
        return tieneAcceso(LocalDateTime.now());
    }

    /**
     * Indica si la fecha de vencimiento del acceso ya pasó (una inscripción que todavía no se habilitó no tiene fecha).
     *
     * @param ahora El momento de referencia.
     * @return {@code true} si el vencimiento es anterior a ese momento.
     */
    public boolean vencimientoPasado(LocalDateTime ahora) {
        return fechaVencimientoAcceso != null && fechaVencimientoAcceso.isBefore(ahora);
    }

    /**
     * Indica si puede entrar al contenido ahora: tiene el acceso habilitado y el dictado de la cohorte ya comenzó (o no tiene fechas de dictado).
     *
     * @param ahora El momento de referencia.
     * @return {@code true} si puede acceder y el dictado ya comenzó.
     */
    public boolean puedeAccederAhora(LocalDateTime ahora) {
        return tieneAcceso(ahora) && !cohorte.dictadoPorComenzar(ahora);
    }

    /**
     * Indica si puede entrar al contenido en este momento.
     *
     * @return {@code true} si puede acceder y el dictado ya comenzó.
     */
    public boolean puedeAccederAhora() {
        return puedeAccederAhora(LocalDateTime.now());
    }

    /**
     * Indica si espera el inicio del dictado en este momento (ver {@link #esperaElDictado(LocalDateTime)}).
     *
     * @return {@code true} si tiene el acceso habilitado pero el dictado no comenzó.
     */
    public boolean esperaElDictado() {
        return esperaElDictado(LocalDateTime.now());
    }

    /**
     * Indica si tiene el acceso habilitado pero el dictado de su cohorte todavía no comenzó (entra cuando empiece).
     *
     * @param ahora El momento de referencia.
     * @return {@code true} si espera el inicio del dictado.
     */
    public boolean esperaElDictado(LocalDateTime ahora) {
        return tieneAcceso(ahora) && cohorte.dictadoPorComenzar(ahora);
    }

    /**
     * Indica si el acceso venció (estado guardado).
     *
     * @return {@code true} si el estado es Acceso vencido.
     */
    public boolean estaVencida() {
        return esEstado(EstadoInscripcion.VENCIDA);
    }

    /**
     * Indica si la inscripción fue dada de baja (por el alumno o el administrador).
     *
     * @return {@code true} si está dada de baja.
     */
    public boolean estaDadaDeBaja() {
        return baja || esEstado(EstadoInscripcion.DADA_DE_BAJA);
    }

    /**
     * Indica si la inscripción sigue en juego para su cohorte: está pendiente de pago, con un pago en curso o con el acceso por vencer (habilitada
     * o suspendida, con la fecha de vencimiento por delante). Cuando ninguna lo está, la cursada de la cohorte terminó.
     *
     * @param ahora El momento de referencia.
     * @return {@code true} si todavía puede pasar algo con ella.
     */
    public boolean sigueEnJuego(LocalDateTime ahora) {
        if (estaDadaDeBaja() || estaCancelada()) {
            return false;
        }
        if (requierePago()) {
            return true;
        }
        return (esEstado(EstadoInscripcion.HABILITADA) || esEstado(EstadoInscripcion.SUSPENDIDA) || estadoInscripcion == null)
                && !vencimientoPasado(ahora);
    }

    /**
     * Indica si el estado ya no puede cambiar: Cancelada por falta de pago y Dada de baja son finales.
     *
     * @return {@code true} si la inscripción es inmutable.
     */
    public boolean esInmutable() {
        return estaCancelada() || estaDadaDeBaja();
    }

    /**
     * Indica si el administrador le suspendió el acceso.
     *
     * @return {@code true} si el estado es Suspendida.
     */
    public boolean estaSuspendida() {
        return esEstado(EstadoInscripcion.SUSPENDIDA);
    }

    /**
     * Indica si la inscripción está pendiente de pago: se inscribió y no tiene un pago en curso ni el acceso habilitado.
     *
     * @return {@code true} si el estado es Pendiente de pago.
     */
    public boolean estaPendienteDePago() {
        return esEstado(EstadoInscripcion.PENDIENTE_DE_PAGO);
    }

    /**
     * Indica si tiene un pago online en curso dentro del plazo de confirmación (reserva el cupo hasta que se resuelva).
     *
     * @return {@code true} si el estado es Pago en curso.
     */
    public boolean estaConPagoEnCurso() {
        return esEstado(EstadoInscripcion.PAGO_EN_CURSO);
    }

    /**
     * Indica si la inscripción fue cancelada (terminó la inscripción sin que se pagara, o la canceló el administrador).
     *
     * @return {@code true} si el estado es Cancelada.
     */
    public boolean estaCancelada() {
        return esEstado(EstadoInscripcion.CANCELADA);
    }

    /**
     * Indica si se la puede cancelar por falta de pago: está pendiente de pago y no tiene un pago en curso ni pendiente. Con un pago parcial aprobado
     * también se cancela (queda registrado, sin devolución). Nunca se cancela una con un pago en curso o pendiente: la plata puede estar por llegar.
     *
     * @return {@code true} si se la puede cancelar por falta de pago.
     */
    public boolean sePuedeCancelarPorFaltaDePago() {
        return estaPendienteDePago() && pagos.stream().noneMatch(Pago::estaPendiente);
    }

    /**
     * Indica si todavía hay que pagarla: está pendiente de pago o con un pago en curso.
     *
     * @return {@code true} si no está habilitada ni cancelada.
     */
    public boolean requierePago() {
        return estaPendienteDePago() || estaConPagoEnCurso();
    }

    /**
     * Indica si la inscripción ocupa un lugar de la cohorte: la habilitada, la suspendida y la vencida (conservan su cupo) y la que tiene un pago
     * en curso (que lo reserva).
     *
     * @return {@code true} si ocupa cupo.
     */
    public boolean ocupaCupo() {
        return fueHabilitada() || estaConPagoEnCurso();
    }

    /**
     * Habilita el acceso. La primera vez que se habilita calcula el vencimiento del acceso a partir de ese momento
     * (ver {@link Cohorte#calcularVencimientoAcceso}); si ya tenía un vencimiento, lo conserva.
     *
     * @param habilitada El estado Habilitada.
     * @param ahora      El momento de la habilitación.
     */
    public void habilitar(EstadoInscripcion habilitada, LocalDateTime ahora) {
        boolean primeraVez = !fueHabilitada() || fechaVencimientoAcceso == null;
        estadoInscripcion = habilitada;
        if (primeraVez) {
            fechaVencimientoAcceso = cohorte.calcularVencimientoAcceso(ahora);
        }
    }

    /**
     * Pasa el avance a Finalizado si el alumno ya completó todos los temas del cronograma.
     *
     * @param finalizado El estado Finalizado.
     * @return {@code true} si quedó finalizada.
     */
    public boolean actualizarAvance(EstadoAvance finalizado) {
        if (getPorcentajeProgreso() >= 100) {
            estadoAvance = finalizado;
            return true;
        }
        return false;
    }

    /**
     * Estado de la inscripción (Pendiente de pago, Pago en curso, Acceso habilitado, Acceso suspendido, Acceso vencido, Cancelada por falta de pago o
     * Dada de baja), distinto del avance.
     *
     * @return El nombre del estado, o «Dada de baja» si el alumno o el administrador la dio de baja.
     */
    public String getNombreEstado() {
        if (baja) {
            return EstadoInscripcion.DADA_DE_BAJA;
        }
        return estadoInscripcion == null ? EstadoInscripcion.HABILITADA : estadoInscripcion.getNombre();
    }

    /**
     * Avance para el administrador: «Sin avance» mientras la inscripción no está habilitada (pendiente de pago, con el pago en
     * curso o cancelada), porque todavía no puede avanzar; después, el avance del alumno.
     *
     * @return «Sin avance», «En progreso» o «Finalizado».
     */
    public String getNombreAvance() {
        return fueHabilitada() ? getEstado() : AVANCE_SIN_AVANCE;
    }

    /**
     * Obtiene el pago aprobado de la inscripción.
     *
     * @return El pago aprobado, o {@code null} si todavía no se pagó.
     */
    public Pago getPagoAprobado() {
        return pagos.stream().filter(Pago::estaAprobado).findFirst().orElse(null);
    }

    /**
     * Suma lo que el alumno ya pagó: los montos de los pagos aprobados.
     *
     * @return El monto pagado.
     */
    public float getMontoPagado() {
        float pagado = 0;
        for (Pago pago : pagos) {
            if (pago.estaAprobado()) {
                pagado += pago.getMonto();
            }
        }
        return pagado;
    }

    /**
     * Indica si la inscripción tiene un pago aprobado.
     *
     * @return {@code true} si está paga.
     */
    public boolean tienePagoAprobado() {
        return getPagoAprobado() != null;
    }

    /**
     * Obtiene el último pago de la inscripción que no está aprobado: el pendiente o el rechazado que se puede reintentar.
     *
     * @return El pago, o {@code null} si no tiene.
     */
    public Pago getPagoSinAprobar() {
        return pagos.stream().filter(pago -> !pago.estaAprobado())
                .max(Comparator.comparing(Pago::getFechaCreacion).thenComparingInt(Pago::getIdPago)).orElse(null);
    }

    /**
     * Obtiene el pago que está en curso: el último pago sin aprobar, si todavía está pendiente de confirmación.
     *
     * @return El pago pendiente, o {@code null} si no hay ninguno en curso.
     */
    public Pago getPagoEnCurso() {
        Pago pago = getPagoSinAprobar();
        return pago != null && pago.estaPendiente() ? pago : null;
    }

    /**
     * Obtiene el certificado vigente (no anulado) de la inscripción.
     *
     * @return El certificado, o {@code null} si no se emitió.
     */
    public Certificado getCertificadoVigente() {
        return certificados.stream().filter(certificado -> !certificado.estaAnulado())
                .max(Comparator.comparing(Certificado::getFechaCreacion)).orElse(null);
    }

    /**
     * Calcula la fecha de vencimiento más temprana que puede tener el acceso: la que resulta de la duración del
     * curso, contada desde el inicio del dictado o, si no tiene, desde la fecha de la inscripción.
     *
     * @return La fecha de vencimiento mínima.
     */
    public LocalDateTime getVencimientoMinimo() {
        LocalDateTime base = cohorte.getFechaInicioDictado() != null ? cohorte.getFechaInicioDictado() : fechaCreacion;
        return base.plusDays(cohorte.getCurso().getDuracionDias());
    }

    /**
     * Obtiene las autoevaluaciones que el alumno todavía tiene para hacer en esta inscripción: las visibles y abiertas de los temas que ya puede
     * cursar y no completó, que no aprobó y a las que todavía le quedan intentos. Solo cuenta con el acceso vigente.
     *
     * @return Las autoevaluaciones pendientes, en el orden del cronograma.
     */
    public List<Autoevaluacion> getAutoevaluacionesPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        if (!puedeAccederAhora(ahora) || cohorte.getPrograma() == null) {
            return List.of();
        }
        List<Autoevaluacion> pendientes = new java.util.ArrayList<>();
        for (TemaCronograma cronograma : cohorte.getPrograma().getCronogramaOrdenado()) {
            if (estaTemaCompletada(cronograma.getTema()) || !estaTemaHabilitada(cronograma)) {
                continue;
            }
            for (Autoevaluacion autoevaluacion : cronograma.getTema().getAutoevaluacionesPublicadas()) {
                Integer restantes = autoevaluacion.intentosRestantes(this);
                if (autoevaluacion.estaAbierta(ahora) && !autoevaluacion.estaAprobadaPor(this) && (restantes == null || restantes > 0)) {
                    pendientes.add(autoevaluacion);
                }
            }
        }
        return pendientes;
    }

    /**
     * Cuenta los temas del cronograma del programa que el alumno ya completó.
     *
     * @return La cantidad de temas completados.
     */
    public int getTemasCompletadas() {
        return (int) progresos.stream().filter(Progreso::getCompletada).count();
    }

    /**
     * Calcula el progreso general del alumno en el curso, según los temas completados.
     *
     * @return El porcentaje de progreso (0-100).
     */
    public int getPorcentajeProgreso() {
        int total = cohorte.getPrograma().getCantidadTemas();
        return total == 0 ? 0 : Math.min(100, getTemasCompletadas() * 100 / total);
    }

    /**
     * Calcula el avance de la inscripción: En progreso (aunque todavía no haya completado ningún tema) o Finalizado.
     *
     * @return El estado de la inscripción.
     */
    public String getEstado() {
        if (estadoAvance != null) {
            return estadoAvance.getNombre();
        }
        return getPorcentajeProgreso() >= 100 ? EstadoAvance.FINALIZADO : EstadoAvance.EN_PROGRESO;
    }

    /**
     * Indica si el alumno completó un tema.
     *
     * @param tema El tema a consultar.
     * @return {@code true} si el tema figura como completada.
     */
    public boolean estaTemaCompletada(Tema tema) {
        return progresos.stream()
                .anyMatch(progreso -> progreso.getCompletada() && progreso.getTema().getIdTema() == tema.getIdTema());
    }

    /**
     * Indica si un tema está habilitada según el avance secuencial del alumno: es la primera del cronograma o
     * el tema anterior figura como completada.
     *
     * @param temaCronograma El tema del cronograma a consultar.
     * @return {@code true} si el alumno puede acceder al tema.
     */
    public boolean estaTemaHabilitada(TemaCronograma temaCronograma) {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getNumeroOrden() < temaCronograma.getNumeroOrden())
                .max(Comparator.comparingInt(TemaCronograma::getNumeroOrden))
                .map(anterior -> estaTemaCompletada(anterior.getTema())).orElse(true);
    }

    /**
     * Obtiene el tema que el alumno debe cursar ahora: la primera del cronograma que todavía no completó.
     *
     * @return El tema en curso, o {@code null} si completó todas.
     */
    public TemaCronograma getTemaEnCurso() {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> !estaTemaCompletada(cronograma.getTema())).findFirst().orElse(null);
    }

    /**
     * Calcula el estado de un tema para el alumno: Completada, En curso (habilitada y sin completar) o Bloqueada.
     *
     * @param temaCronograma El tema del cronograma a consultar.
     * @return El estado del tema.
     */
    public String getEstadoTema(TemaCronograma temaCronograma) {
        if (estaTemaCompletada(temaCronograma.getTema())) {
            return "Completada";
        }
        return estaTemaHabilitada(temaCronograma) ? "En curso" : "Bloqueada";
    }

    /**
     * Obtiene el número de orden del último tema que el alumno completó.
     *
     * @return El número de orden, o 0 si no completó ninguna.
     */
    public int getUltimaTemaCompletada() {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> estaTemaCompletada(cronograma.getTema()))
                .mapToInt(TemaCronograma::getNumeroOrden).max().orElse(0);
    }

    /**
     * Obtiene el número de orden del último tema que, según el cronograma, ya debería estar terminada hoy.
     *
     * @return El número de orden, o 0 si todavía no terminó ningún tema.
     */
    public int getTemasEsperadasTerminadas() {
        Programa programa = cohorte.getPrograma();
        return programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> programa.getDiaHasta(cronograma) < cohorte.getDiasTranscurridos())
                .mapToInt(TemaCronograma::getNumeroOrden).max().orElse(0);
    }

    /**
     * Indica si el alumno va por detrás de lo esperado: su último tema completado es anterior a la última que
     * el cronograma ya daba por terminada.
     *
     * @return {@code true} si el alumno está atrasado.
     */
    public boolean estaAtrasada() {
        return getUltimaTemaCompletada() < getTemasEsperadasTerminadas();
    }

    /**
     * Calcula cuántos días de atraso lleva el alumno: la diferencia entre el día actual de la cohorte y el
     * último día del primer tema que todavía no completó, si ese tema ya debería estar terminada.
     *
     * @return Los días de atraso, o 0 si el alumno está al día.
     */
    public int getDiasDeAtraso() {
        TemaCronograma pendiente = getTemaEnCurso();
        if (pendiente == null) {
            return 0;
        }
        return Math.max(0, cohorte.getDiasTranscurridos() - pendiente.getDiaHasta());
    }

    /**
     * Calcula el estado resumido de un tema según el avance del alumno: Completada, En curso (la primera que no
     * completó) o Pendiente.
     *
     * @param temaCronograma El tema del cronograma a consultar.
     * @return "Completada", "En curso" o "Pendiente".
     */
    public String getEstadoTemaResumido(TemaCronograma temaCronograma) {
        if (estaTemaCompletada(temaCronograma.getTema())) {
            return "Completada";
        }
        TemaCronograma enCurso = getTemaEnCurso();
        return enCurso != null && enCurso.getNumeroOrden() == temaCronograma.getNumeroOrden()
                ? "En curso" : "Pendiente";
    }

    /**
     * Calcula el estado de un tema respecto del cronograma: Completada, Atrasada (debería estar terminada),
     * En curso o Próxima.
     *
     * @param temaCronograma El tema del cronograma a consultar.
     * @return El estado del tema en el cronograma.
     */
    public String getEstadoCronograma(TemaCronograma temaCronograma) {
        if (estaTemaCompletada(temaCronograma.getTema())) {
            return "Completada";
        }
        int dia = cohorte.getDiasTranscurridos();
        if (temaCronograma.getDiaHasta() < dia) {
            return "Atrasada";
        }
        return temaCronograma.getDiaDesde() <= dia ? "Actual" : "Próxima";
    }
}
