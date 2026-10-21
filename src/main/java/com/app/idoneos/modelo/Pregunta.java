package com.app.idoneos.modelo;

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
 * Pregunta de un pool, con opciones de respuesta.
 */
@Entity
@Table(name = "Pregunta")
@Getter
@Setter
@NoArgsConstructor
public class Pregunta {

    /**
     * Identificador único de la pregunta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPregunta")
    private int idPregunta;

    /**
     * Relación con Pool.
     */
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /**
     * Texto de la pregunta.
     */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /**
     * Indica si la pregunta admite más de una opción correcta.
     */
    @Column(name = "esOpcionMultiple", nullable = false)
    private Boolean esOpcionMultiple = false;

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
     * Conjunto de opcionesRespuesta asociados.
     */
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private Set<OpcionRespuesta> opcionesRespuesta = new HashSet<>();

    /**
     * Conjunto de preguntasIntento asociados.
     */
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private Set<PreguntaIntento> preguntasIntento = new HashSet<>();

    /**
     * Constructor para crear la pregunta con los datos básicos.
     *
     * @param pool relación con Pool.
     * @param texto texto de la pregunta.
     * @param esOpcionMultiple indica si la pregunta admite más de una opción correcta.
     */
    public Pregunta(Pool pool, String texto, Boolean esOpcionMultiple) {
        this.pool = pool;
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
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
     * Obtiene las opciones de respuesta vigentes de la pregunta.
     *
     * @return Las opciones, en el orden en que se cargaron.
     */
    public List<OpcionRespuesta> getOpcionesActivas() {
        return opcionesRespuesta.stream().filter(opcion -> !opcion.getBaja())
                .sorted(Comparator.comparingInt(OpcionRespuesta::getIdOpcionRespuesta)).toList();
    }

    /**
     * Cuenta las opciones vigentes marcadas como correctas.
     *
     * @return La cantidad de opciones correctas.
     */
    public int getCantidadOpcionesCorrectas() {
        return (int) opcionesRespuesta.stream().filter(opcion -> !opcion.getBaja() && opcion.getEsCorrecta()).count();
    }

    /**
     * Indica si la pregunta admite una única opción correcta (opción única y verdadero/falso).
     *
     * @return {@code true} si no es de opción múltiple.
     */
    public boolean esOpcionUnica() {
        return !esOpcionMultiple;
    }

    /**
     * Indica si la pregunta tiene al menos dos opciones y la cantidad de opciones correctas que corresponde a su tipo:
     * exactamente una en las de opción única y al menos una en las de opción múltiple.
     *
     * @return {@code true} si la pregunta puede usarse en un pool.
     */
    public boolean esValida() {
        int correctas = getCantidadOpcionesCorrectas();
        boolean correctasValidas = esOpcionMultiple ? correctas >= 1 : correctas == 1;
        return getOpcionesActivas().size() >= 2 && correctasValidas;
    }

    /**
     * Calcula cuánto suma o resta a la nota del intento una opción de la pregunta cuando el alumno la elige. Cada pregunta
     * vale lo mismo (1 / cantidad de preguntas) y ese valor se reparte entre sus opciones correctas; una opción incorrecta
     * resta ese mismo valor si la autoevaluación descuenta por error, y si no, no modifica la nota.
     *
     * @param esCorrecta Si la opción elegida es correcta.
     * @param descuentoPorError Si la autoevaluación descuenta las respuestas incorrectas.
     * @param cantidadPreguntas La cantidad de preguntas de la autoevaluación.
     * @return El valor de la opción, sobre una nota de 0 a 1.
     */
    public double calcularValorOpcion(boolean esCorrecta, boolean descuentoPorError, int cantidadPreguntas) {
        int cantidadCorrectas = getCantidadOpcionesCorrectas();
        if (cantidadPreguntas <= 0 || cantidadCorrectas == 0) {
            return 0;
        }
        double valorOpcion = (1d / cantidadPreguntas) / cantidadCorrectas;
        if (esCorrecta) {
            return valorOpcion;
        }
        return descuentoPorError ? -valorOpcion : 0;
    }

    /**
     * Devuelve una representación en forma de cadena de la pregunta.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Pregunta #" + idPregunta;
    }
}
