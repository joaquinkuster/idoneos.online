package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una pregunta de un pool, con sus opciones de respuesta. */
@Entity
@Table(name = "Pregunta")
@Check(name = "ck_pregunta_texto", constraints = "trim(texto) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Pregunta {

    /** Identificador único de la pregunta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPregunta")
    private int idPregunta;

    /** Pool de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /** Texto de la pregunta. */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /** Indica si la pregunta admite más de una opción correcta. */
    @Column(name = "esOpcionMultiple", nullable = false)
    private Boolean esOpcionMultiple = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Opciones respuesta que referencian a este registro. */
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private Set<OpcionRespuesta> opcionesRespuesta = new HashSet<>();

    /** Preguntas intento que referencian a este registro. */
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private Set<PreguntaIntento> preguntasIntento = new HashSet<>();

    /** Crea la pregunta con los datos básicos. */
    public Pregunta(Pool pool, String texto, Boolean esOpcionMultiple) {
        this.pool = pool;
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Obtiene las opciones de respuesta vigentes de la pregunta. */
    public List<OpcionRespuesta> getOpcionesActivas() {
        return opcionesRespuesta.stream().filter(opcion -> !opcion.getBaja())
                .sorted(Comparator.comparingInt(OpcionRespuesta::getIdOpcionRespuesta)).toList();
    }

    /** Cuenta las opciones vigentes marcadas como correctas. */
    public int getCantidadOpcionesCorrectas() {
        return (int) opcionesRespuesta.stream().filter(opcion -> !opcion.getBaja() && opcion.getEsCorrecta()).count();
    }

    /** Indica si la pregunta admite una única opción correcta (opción única y verdadero/falso). */
    public boolean esOpcionUnica() {
        return !esOpcionMultiple;
    }

    /** Indica si la pregunta tiene al menos dos opciones y la cantidad de opciones correctas que corresponde a su tipo: exactamente una en las de opción única y al menos una en las de opción múltiple. */
    public boolean esValida() {
        int correctas = getCantidadOpcionesCorrectas();
        boolean correctasValidas = esOpcionMultiple ? correctas >= 1 : correctas == 1;
        return getOpcionesActivas().size() >= 2 && correctasValidas;
    }

    /** Calcula cuánto suma o resta a la nota del intento una opción de la pregunta cuando el alumno la elige. */
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

    /** Devuelve una representación en forma de cadena de la pregunta. */
    @Override
    public String toString() {
        return "Pregunta #" + idPregunta;
    }
}
