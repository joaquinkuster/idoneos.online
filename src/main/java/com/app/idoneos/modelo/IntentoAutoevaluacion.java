package com.app.idoneos.modelo;

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

/**
 * Intento de un alumno de resolver una autoevaluación, con su nota y resultado.
 */
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

    /**
     * Identificador único del intento de autoevaluación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIntentoAutoevaluacion")
    private int idIntentoAutoevaluacion;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Relación con Autoevaluacion.
     */
    @ManyToOne
    @JoinColumn(name = "idAutoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    /**
     * Nota obtenida. Es opcional hasta que se entrega el intento.
     */
    @Column(name = "nota", nullable = true)
    private Float nota;

    /**
     * Indica si el intento fue aprobado.
     */
    @Column(name = "aprobado", nullable = false)
    private Boolean aprobado = false;

    /**
     * Fecha y hora de entrega del intento. Es opcional.
     */
    @Column(name = "fechaEntrega", nullable = true)
    private LocalDateTime fechaEntrega;

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
     * Conjunto de preguntasIntento asociados.
     */
    @OneToMany(mappedBy = "intentoAutoevaluacion", cascade = CascadeType.ALL)
    private Set<PreguntaIntento> preguntasIntento = new HashSet<>();

    /**
     * Constructor para crear el intento de autoevaluación con los datos básicos.
     *
     * @param inscripcion relación con Inscripcion.
     * @param autoevaluacion relación con Autoevaluacion.
     */
    public IntentoAutoevaluacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion) {
        this.inscripcion = inscripcion;
        this.autoevaluacion = autoevaluacion;
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
     * Indica si el intento está en curso: el alumno lo inició y todavía no lo entregó ni se cerró por tiempo.
     *
     * @return {@code true} si el intento está vigente y sin entregar.
     */
    public boolean estaEnCurso() {
        return !baja && fechaEntrega == null;
    }

    /**
     * Indica si el intento ya fue entregado (por el alumno o al agotarse el tiempo) y corregido.
     *
     * @return {@code true} si tiene fecha de entrega.
     */
    public boolean estaFinalizado() {
        return fechaEntrega != null;
    }

    /**
     * Calcula el momento en que se agota el tiempo del intento: su inicio más el tiempo límite de la autoevaluación. El
     * tiempo corre sin pausarse, aunque el alumno salga del cuestionario.
     *
     * @return El vencimiento, o {@code null} si la autoevaluación no tiene tiempo límite.
     */
    public LocalDateTime getVencimiento() {
        Integer minutos = autoevaluacion.getTiempoLimite();
        return minutos == null ? null : fechaCreacion.plusMinutes(minutos);
    }

    /**
     * Calcula cuántos segundos le quedan al intento.
     *
     * @param momento La fecha y hora de referencia (la del servidor).
     * @return Los segundos restantes (nunca negativos), o {@code null} si no hay tiempo límite.
     */
    public Long getSegundosRestantes(LocalDateTime momento) {
        LocalDateTime vencimiento = getVencimiento();
        return vencimiento == null ? null : Math.max(0, Duration.between(momento, vencimiento).getSeconds());
    }

    /**
     * Indica si pasó el tiempo del intento más la tolerancia que se admite para las entregas que llegan con demora.
     *
     * @param momento La fecha y hora de referencia (la del servidor).
     * @param graciaSegundos La tolerancia, en segundos.
     * @return {@code true} si ya no se admiten respuestas ni entregas.
     */
    public boolean haVencido(LocalDateTime momento, int graciaSegundos) {
        LocalDateTime vencimiento = getVencimiento();
        return vencimiento != null && momento.isAfter(vencimiento.plusSeconds(graciaSegundos));
    }

    /**
     * Obtiene las preguntas sorteadas para el intento.
     *
     * @return Las preguntas, en el orden del cuestionario.
     */
    public List<PreguntaIntento> getPreguntasOrdenadas() {
        return preguntasIntento.stream().sorted(Comparator.comparingInt(PreguntaIntento::getOrden)).toList();
    }

    /**
     * Corrige el intento según las opciones que el alumno eligió: suma el valor de cada opción elegida (ver
     * {@link Pregunta#calcularValorOpcion}), sin permitir que la nota resulte menor a cero, la expresa en una escala de 0 a 10
     * con dos decimales y determina si alcanza la nota mínima. Las preguntas sin responder no suman ni restan.
     */
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

    /**
     * Indica si el intento se aprobó manualmente: está aprobado con una nota inferior a la mínima. La nota mínima no puede
     * modificarse una vez que la autoevaluación tiene intentos, por eso este dato se deduce sin guardarse.
     *
     * @return {@code true} si el docente aprobó el intento.
     */
    public boolean esAprobadoPorDocente() {
        return aprobado && nota != null && nota < autoevaluacion.getNotaMinima();
    }

    /**
     * Obtiene el resultado del intento: En curso, Aprobado, Aprobado por el docente o No aprobado.
     *
     * @return El resultado, para mostrarlo.
     */
    public String getResultado() {
        if (!estaFinalizado()) {
            return RESULTADO_EN_CURSO;
        }
        if (!aprobado) {
            return RESULTADO_NO_APROBADO;
        }
        return esAprobadoPorDocente() ? RESULTADO_APROBADO_POR_DOCENTE : RESULTADO_APROBADO;
    }

    /**
     * Devuelve una representación en forma de cadena del intento de autoevaluación.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "IntentoAutoevaluacion #" + idIntentoAutoevaluacion;
    }
}
