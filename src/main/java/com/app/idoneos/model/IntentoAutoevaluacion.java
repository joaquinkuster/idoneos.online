package com.app.idoneos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un intento de un alumno de resolver una autoevaluación, con su nota y resultado. */
@Entity
@Table(name = "IntentoAutoevaluacion")
@Getter
@Setter
@NoArgsConstructor
public class IntentoAutoevaluacion {

    /** Resultado de un intento que el alumno todavía no entregó. */
    public static final String RESULTADO_EN_CURSO = "En curso";

    /** Resultado de un intento que alcanzó la nota mínima. */
    public static final String RESULTADO_APROBADO = "Aprobado";

    /** Resultado de un intento que el docente aprobó manualmente. */
    public static final String RESULTADO_APROBADO_POR_DOCENTE = "Aprobado por el docente";

    /** Resultado de un intento que no alcanzó la nota mínima. */
    public static final String RESULTADO_NO_APROBADO = "No aprobado";

    /** Identificador único del intento de autoevaluación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIntentoAutoevaluacion")
    private int idIntentoAutoevaluacion;

    /** Inscripcion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /** Autoevaluacion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idAutoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    /** Nota obtenida. */
    @Column(name = "nota", nullable = true)
    private Float nota;

    /** Indica si el intento fue aprobado. */
    @Column(name = "aprobado", nullable = false)
    private Boolean aprobado = false;

    /** Fecha y hora de entrega del intento. */
    @Column(name = "fechaEntrega", nullable = true)
    private LocalDateTime fechaEntrega;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Preguntas intento que referencian a este registro. */
    @OneToMany(mappedBy = "intentoAutoevaluacion", cascade = CascadeType.ALL)
    private Set<PreguntaIntento> preguntasIntento = new HashSet<>();

    /** Crea el intento de autoevaluación con los datos básicos. */
    public IntentoAutoevaluacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion) {
        this.inscripcion = inscripcion;
        this.autoevaluacion = autoevaluacion;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Indica si el intento está en curso: el alumno lo inició y todavía no lo entregó ni se cerró por tiempo. */
    public boolean estaEnCurso() {
        return !baja && fechaEntrega == null;
    }

    /** Indica si el intento ya fue entregado (por el alumno o al agotarse el tiempo) y corregido. */
    public boolean estaFinalizado() {
        return fechaEntrega != null;
    }

    /** Calcula el momento en que se agota el tiempo del intento. */
    public LocalDateTime getVencimiento() {
        Integer minutos = autoevaluacion.getTiempoLimite();
        return minutos == null ? null : fechaCreacion.plusMinutes(minutos);
    }

    /** Expresa cuánto tardó el alumno en entregar el intento, para mostrarlo: en minutos y, si no llegó al minuto, en segundos. */
    public String getDuracionTexto() {
        if (fechaEntrega == null) {
            return null;
        }
        long segundos = Math.max(0, Duration.between(fechaCreacion, fechaEntrega).getSeconds());
        return segundos < 60 ? segundos + " s" : (segundos / 60) + " min";
    }

    /** Calcula cuántos minutos (con decimales) tardó el alumno en entregar el intento, para ordenar y filtrar. */
    public Double getDuracionEnMinutos() {
        return fechaEntrega == null ? null
                : Math.round(Math.max(0, Duration.between(fechaCreacion, fechaEntrega).getSeconds()) / 60.0 * 100) / 100.0;
    }

    /** Calcula cuánto tardó el alumno en entregar el intento. */
    public Long getDuracionMinutos() {
        return fechaEntrega == null ? null : Duration.between(fechaCreacion, fechaEntrega).toMinutes();
    }

    /** Calcula cuántos segundos le quedan al intento. */
    public Long getSegundosRestantes(LocalDateTime momento) {
        LocalDateTime vencimiento = getVencimiento();
        return vencimiento == null ? null : Math.max(0, Duration.between(momento, vencimiento).getSeconds());
    }

    /** Indica si pasó el tiempo del intento más la tolerancia que se admite para las entregas que llegan con demora. */
    public boolean haVencido(LocalDateTime momento, int graciaSegundos) {
        LocalDateTime vencimiento = getVencimiento();
        return vencimiento != null && momento.isAfter(vencimiento.plusSeconds(graciaSegundos));
    }

    /** Obtiene las preguntas sorteadas para el intento. */
    public List<PreguntaIntento> getPreguntasOrdenadas() {
        return preguntasIntento.stream().sorted(Comparator.comparingInt(PreguntaIntento::getOrden)).toList();
    }

    /** Obtiene la pregunta en la que está el alumno en un cuestionario secuencial. */
    public PreguntaIntento getPreguntaActualSecuencial() {
        return getPreguntasOrdenadas().stream().filter(p -> !p.getPasada()).findFirst().orElse(null);
    }

    /** Cuenta las preguntas con al menos una opción elegida. */
    public int getCantidadRespondidas() {
        return (int) preguntasIntento.stream().filter(PreguntaIntento::estaRespondida).count();
    }

    /** Cuenta las preguntas que el alumno marcó para revisar. */
    public int getCantidadMarcadas() {
        return (int) preguntasIntento.stream().filter(PreguntaIntento::getMarcada).count();
    }

    /** Corrige el intento según las opciones que el alumno eligió. */
    public void corregir() {
        boolean descuentoPorError = autoevaluacion.getDescuentoPorError();
        int cantidadPreguntas = autoevaluacion.getCantidadPreguntas();
        double suma = 0;
        for (PreguntaIntento preguntaIntento : preguntasIntento) {
            for (RespuestaIntento respuesta : preguntaIntento.getRespuestasIntento()) {
                suma += respuesta.calcularValorOpcion(descuentoPorError, cantidadPreguntas);
            }
        }
        nota = BigDecimal.valueOf(Math.max(0, suma) * 10).setScale(2, RoundingMode.HALF_UP).floatValue();
        aprobado = nota >= autoevaluacion.getNotaMinima();
    }

    /** Indica si el intento se aprobó manualmente: está aprobado con una nota inferior a la mínima. */
    public boolean esAprobadoPorDocente() {
        return aprobado && nota != null && nota < autoevaluacion.getNotaMinima();
    }

    /** Obtiene el resultado del intento: En curso, Aprobado, Aprobado por el docente o No aprobado. */
    public String getResultado() {
        if (!estaFinalizado()) {
            return RESULTADO_EN_CURSO;
        }
        if (!aprobado) {
            return RESULTADO_NO_APROBADO;
        }
        return esAprobadoPorDocente() ? RESULTADO_APROBADO_POR_DOCENTE : RESULTADO_APROBADO;
    }

    /** Devuelve una representación en forma de cadena del intento de autoevaluación. */
    @Override
    public String toString() {
        return "IntentoAutoevaluacion #" + idIntentoAutoevaluacion;
    }
}
