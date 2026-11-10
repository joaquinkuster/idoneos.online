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

/** Entidad que representa la inscripción de un alumno a una cohorte. */
@Entity
@Table(name = "Inscripcion")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    /** Identificador único de la inscripción. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscripcion")
    private int idInscripcion;

    /** Cohorte de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    /** Alumno de este registro. */
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /** Fecha y hora en que vence el acceso al contenido. */
    @Column(name = "fechaVencimientoAcceso", nullable = true)
    private LocalDateTime fechaVencimientoAcceso;

    /** Fecha y hora en que el alumno aceptó los términos y condiciones al inscribirse. */
    @Column(name = "fechaAceptacionTyc", nullable = false)
    private LocalDateTime fechaAceptacionTyc = LocalDateTime.now();

    /** Observaciones sobre la inscripción. */
    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Estado del ciclo de vida de la inscripción: Pendiente de pago, Pago en curso, Habilitada. */
    @ManyToOne
    @JoinColumn(name = "idEstadoInscripcion", nullable = false)
    private EstadoInscripcion estadoInscripcion;

    /** Estado del avance del alumno: En progreso o Finalizado (completó todos los temas del cronograma). */
    @ManyToOne
    @JoinColumn(name = "idEstadoAvance", nullable = false)
    private EstadoAvance estadoAvance;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Progresos que referencian a este registro. */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Progreso> progresos = new HashSet<>();

    /** Consultas foro que referencian a este registro. */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<ConsultaForo> consultasForo = new HashSet<>();

    /** Pagos que referencian a este registro. */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    /** Intentos autoevaluacion que referencian a este registro. */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<IntentoAutoevaluacion> intentosAutoevaluacion = new HashSet<>();

    /** Certificados que referencian a este registro. */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Certificado> certificados = new HashSet<>();

    /** Crea la inscripción con los datos básicos. */
    public Inscripcion(Cohorte cohorte, Alumno alumno, LocalDateTime fechaVencimientoAcceso) {
        this.cohorte = cohorte;
        this.alumno = alumno;
        this.fechaVencimientoAcceso = fechaVencimientoAcceso;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la inscripción. */
    @Override
    public String toString() {
        return "Inscripcion #" + idInscripcion;
    }

    /** Obtiene el curso al que pertenece la inscripción, a través de su cohorte y programa. */
    public Curso getCurso() {
        return cohorte.getPrograma().getCurso();
    }

    public static final String AVANCE_SIN_AVANCE = "Sin avance";

    private boolean esEstado(String nombre) {
        return estadoInscripcion != null && nombre.equals(estadoInscripcion.getNombre());
    }

    /** Indica si la inscripción ya fue habilitada alguna vez: pagó, es gratuita o la habilitó el administrador. */
    public boolean fueHabilitada() {
        return estadoInscripcion == null || esEstado(EstadoInscripcion.HABILITADA) || esEstado(EstadoInscripcion.SUSPENDIDA)
                || esEstado(EstadoInscripcion.VENCIDA);
    }

    /** Indica si el alumno tiene acceso al contenido en un momento. */
    public boolean tieneAcceso(LocalDateTime ahora) {
        return (estadoInscripcion == null || esEstado(EstadoInscripcion.HABILITADA)) && !vencimientoPasado(ahora);
    }

    /** Indica si el alumno tiene acceso al contenido en este momento. */
    public boolean tieneAcceso() {
        return tieneAcceso(LocalDateTime.now());
    }

    /** Indica si la fecha de vencimiento del acceso ya pasó (una inscripción que todavía no se habilitó no tiene fecha). */
    public boolean vencimientoPasado(LocalDateTime ahora) {
        return fechaVencimientoAcceso != null && fechaVencimientoAcceso.isBefore(ahora);
    }

    /** Indica si puede entrar al contenido ahora. */
    public boolean puedeAccederAhora(LocalDateTime ahora) {
        return tieneAcceso(ahora) && !cohorte.dictadoPorComenzar(ahora);
    }

    /** Indica si puede entrar al contenido en este momento. */
    public boolean puedeAccederAhora() {
        return puedeAccederAhora(LocalDateTime.now());
    }

    /** Indica si espera el inicio del dictado en este momento (ver.esperaElDictado(LocalDateTime)). */
    public boolean esperaElDictado() {
        return esperaElDictado(LocalDateTime.now());
    }

    /** Indica si tiene el acceso habilitado pero el dictado de su cohorte todavía no comenzó (entra cuando empiece). */
    public boolean esperaElDictado(LocalDateTime ahora) {
        return tieneAcceso(ahora) && cohorte.dictadoPorComenzar(ahora);
    }

    /** Indica si el acceso venció (estado guardado). */
    public boolean estaVencida() {
        return esEstado(EstadoInscripcion.VENCIDA);
    }

    /** Indica si la inscripción fue dada de baja (por el alumno o el administrador). */
    public boolean estaDadaDeBaja() {
        return baja || esEstado(EstadoInscripcion.DADA_DE_BAJA);
    }

    /** Indica si la inscripción sigue en juego para su cohorte: está pendiente de pago. */
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

    /** Indica si el estado ya no puede cambiar: Cancelada por falta de pago y Dada de baja son finales. */
    public boolean esInmutable() {
        return estaCancelada() || estaDadaDeBaja();
    }

    /** Indica si el administrador le suspendió el acceso. */
    public boolean estaSuspendida() {
        return esEstado(EstadoInscripcion.SUSPENDIDA);
    }

    /** Indica si la inscripción está pendiente de pago. */
    public boolean estaPendienteDePago() {
        return esEstado(EstadoInscripcion.PENDIENTE_DE_PAGO);
    }

    /** Indica si tiene un pago online en curso dentro del plazo de confirmación (reserva el cupo hasta que se resuelva). */
    public boolean estaConPagoEnCurso() {
        return esEstado(EstadoInscripcion.PAGO_EN_CURSO);
    }

    /** Indica si la inscripción fue cancelada. */
    public boolean estaCancelada() {
        return esEstado(EstadoInscripcion.CANCELADA);
    }

    /** Indica si se la puede cancelar por falta de pago. */
    public boolean sePuedeCancelarPorFaltaDePago() {
        return estaPendienteDePago() && pagos.stream().noneMatch(Pago::estaPendiente);
    }

    /** Indica si todavía hay que pagarla: está pendiente de pago o con un pago en curso. */
    public boolean requierePago() {
        return estaPendienteDePago() || estaConPagoEnCurso();
    }

    /** Indica si la inscripción ocupa un lugar de la cohorte: la habilitada. */
    public boolean ocupaCupo() {
        return fueHabilitada() || estaConPagoEnCurso();
    }

    /** Habilita el acceso. */
    public void habilitar(EstadoInscripcion habilitada, LocalDateTime ahora) {
        boolean primeraVez = !fueHabilitada() || fechaVencimientoAcceso == null;
        estadoInscripcion = habilitada;
        if (primeraVez) {
            fechaVencimientoAcceso = cohorte.calcularVencimientoAcceso(ahora);
        }
    }

    /** Pasa el avance a Finalizado si el alumno ya completó todos los temas del cronograma. */
    public boolean actualizarAvance(EstadoAvance finalizado) {
        if (getPorcentajeProgreso() >= 100) {
            estadoAvance = finalizado;
            return true;
        }
        return false;
    }

    /** Obtiene el nombre del estado de la inscripción. */
    public String getNombreEstado() {
        if (baja) {
            return EstadoInscripcion.DADA_DE_BAJA;
        }
        return estadoInscripcion == null ? EstadoInscripcion.HABILITADA : estadoInscripcion.getNombre();
    }

    /** Obtiene el avance para el administrador, que es "Sin avance" mientras la inscripción no está habilitada. */
    public String getNombreAvance() {
        return fueHabilitada() ? getEstado() : AVANCE_SIN_AVANCE;
    }

    /** Obtiene el pago aprobado de la inscripción. */
    public Pago getPagoAprobado() {
        return pagos.stream().filter(Pago::estaAprobado).findFirst().orElse(null);
    }

    /** Suma lo que el alumno ya pagó: los montos de los pagos aprobados. */
    public float getMontoPagado() {
        float pagado = 0;
        for (Pago pago : pagos) {
            if (pago.estaAprobado()) {
                pagado += pago.getMonto();
            }
        }
        return pagado;
    }

    /** Indica si la inscripción tiene un pago aprobado. */
    public boolean tienePagoAprobado() {
        return getPagoAprobado() != null;
    }

    /** Obtiene el último pago de la inscripción que no está aprobado. */
    public Pago getPagoSinAprobar() {
        return pagos.stream().filter(pago -> !pago.estaAprobado())
                .max(Comparator.comparing(Pago::getFechaCreacion).thenComparingInt(Pago::getIdPago)).orElse(null);
    }

    /** Obtiene el pago que está en curso: el último pago sin aprobar, si todavía está pendiente de confirmación. */
    public Pago getPagoEnCurso() {
        Pago pago = getPagoSinAprobar();
        return pago != null && pago.estaPendiente() ? pago : null;
    }

    /** Obtiene el certificado vigente (no anulado) de la inscripción. */
    public Certificado getCertificadoVigente() {
        return certificados.stream().filter(certificado -> !certificado.estaAnulado())
                .max(Comparator.comparing(Certificado::getFechaCreacion)).orElse(null);
    }

    /** Calcula la fecha de vencimiento más temprana que puede tener el acceso. */
    public LocalDateTime getVencimientoMinimo() {
        LocalDateTime base = cohorte.getFechaInicioDictado() != null ? cohorte.getFechaInicioDictado() : fechaCreacion;
        return base.plusDays(cohorte.getCurso().getDuracionDias());
    }

    /** Obtiene las autoevaluaciones que el alumno todavía tiene para hacer en esta inscripción. */
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

    /** Cuenta los temas del cronograma del programa que el alumno ya completó. */
    public int getTemasCompletadas() {
        return (int) progresos.stream().filter(Progreso::getCompletada).count();
    }

    /** Calcula el progreso general del alumno en el curso, según los temas completados. */
    public int getPorcentajeProgreso() {
        int total = cohorte.getPrograma().getCantidadTemas();
        return total == 0 ? 0 : Math.min(100, getTemasCompletadas() * 100 / total);
    }

    /** Calcula el avance de la inscripción. */
    public String getEstado() {
        if (estadoAvance != null) {
            return estadoAvance.getNombre();
        }
        return getPorcentajeProgreso() >= 100 ? EstadoAvance.FINALIZADO : EstadoAvance.EN_PROGRESO;
    }

    /** Indica si el alumno completó un tema. */
    public boolean estaTemaCompletada(Tema tema) {
        return progresos.stream()
                .anyMatch(progreso -> progreso.getCompletada() && progreso.getTema().getIdTema() == tema.getIdTema());
    }

    /** Indica si un tema está habilitada según el avance secuencial del alumno. */
    public boolean estaTemaHabilitada(TemaCronograma temaCronograma) {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getNumeroOrden() < temaCronograma.getNumeroOrden())
                .max(Comparator.comparingInt(TemaCronograma::getNumeroOrden))
                .map(anterior -> estaTemaCompletada(anterior.getTema())).orElse(true);
    }

    /** Obtiene el tema que el alumno debe cursar ahora: la primera del cronograma que todavía no completó. */
    public TemaCronograma getTemaEnCurso() {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> !estaTemaCompletada(cronograma.getTema())).findFirst().orElse(null);
    }

    /** Calcula el estado de un tema para el alumno: Completada. */
    public String getEstadoTema(TemaCronograma temaCronograma) {
        if (estaTemaCompletada(temaCronograma.getTema())) {
            return "Completada";
        }
        return estaTemaHabilitada(temaCronograma) ? "En curso" : "Bloqueada";
    }

    /** Obtiene el número de orden del último tema que el alumno completó. */
    public int getUltimaTemaCompletada() {
        return cohorte.getPrograma().getCronogramaOrdenado().stream()
                .filter(cronograma -> estaTemaCompletada(cronograma.getTema()))
                .mapToInt(TemaCronograma::getNumeroOrden).max().orElse(0);
    }

    /** Obtiene el número de orden del último tema que, según el cronograma, ya debería estar terminada hoy. */
    public int getTemasEsperadasTerminadas() {
        Programa programa = cohorte.getPrograma();
        return programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> programa.getDiaHasta(cronograma) < cohorte.getDiasTranscurridos())
                .mapToInt(TemaCronograma::getNumeroOrden).max().orElse(0);
    }

    /** Indica si el alumno va por detrás de lo esperado. */
    public boolean estaAtrasada() {
        return getUltimaTemaCompletada() < getTemasEsperadasTerminadas();
    }

    /** Calcula cuántos días de atraso lleva el alumno. */
    public int getDiasDeAtraso() {
        TemaCronograma pendiente = getTemaEnCurso();
        if (pendiente == null) {
            return 0;
        }
        return Math.max(0, cohorte.getDiasTranscurridos() - pendiente.getDiaHasta());
    }

    /** Calcula el estado resumido de un tema según el avance del alumno: Completada. */
    public String getEstadoTemaResumido(TemaCronograma temaCronograma) {
        if (estaTemaCompletada(temaCronograma.getTema())) {
            return "Completada";
        }
        TemaCronograma enCurso = getTemaEnCurso();
        return enCurso != null && enCurso.getNumeroOrden() == temaCronograma.getNumeroOrden()
                ? "En curso" : "Pendiente";
    }

    /** Calcula el estado de un tema respecto del cronograma: Completada, Atrasada (debería estar terminada). */
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
