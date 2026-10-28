package com.app.idoneos.modelo;

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

/**
 * Consulta realizada por un alumno en el foro de un tema.
 */
@Entity
@Table(name = "ConsultaForo")
@Check(name = "ck_consulta_foro_texto", constraints = "trim(texto) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class ConsultaForo {

    /** Nombre con el que se muestra la consulta de un alumno que dio de baja su inscripción. */
    public static final String AUTOR_ANONIMO = "Alumno anónimo";

    /** Cantidad máxima de caracteres del texto de la consulta. */
    public static final int LONGITUD_TEXTO = 500;

    /** Cantidad de caracteres del texto que se muestran en los listados. */
    private static final int LONGITUD_RESUMEN = 90;

    /**
     * Identificador único de la consulta del foro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConsulta")
    private int idConsulta;

    /**
     * Relación con Tema.
     */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Texto de la consulta.
     */
    @Column(name = "texto", nullable = false, length = LONGITUD_TEXTO)
    private String texto;

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
     * Conjunto de respuestasForo asociados.
     */
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL)
    private Set<RespuestaForo> respuestasForo = new HashSet<>();

    /**
     * Constructor para crear la consulta del foro con los datos básicos.
     *
     * @param tema relación con Tema.
     * @param inscripcion relación con Inscripcion.
     * @param texto texto de la consulta.
     */
    public ConsultaForo(Tema tema, Inscripcion inscripcion, String texto) {
        this.tema = tema;
        this.inscripcion = inscripcion;
        this.texto = texto;
    }

    /**
     * Obtiene las respuestas vigentes de la consulta.
     *
     * @return Las respuestas, de la más antigua a la más reciente.
     */
    public List<RespuestaForo> getRespuestasVigentes() {
        return respuestasForo.stream().filter(respuesta -> !respuesta.getBaja())
                .sorted(Comparator.comparing(RespuestaForo::getFechaCreacion)
                        .thenComparingInt(RespuestaForo::getIdRespuesta))
                .toList();
    }

    /**
     * Obtiene el nombre del alumno que hizo la consulta. Si el alumno dio de baja su inscripción, su participación
     * se conserva de forma anónima.
     *
     * @return El nombre completo del alumno o, si su inscripción está dada de baja, {@link #AUTOR_ANONIMO}.
     */
    public String getAutor() {
        return inscripcion.esInactivo() ? AUTOR_ANONIMO : inscripcion.getAlumno().getUsuario().getNombreCompleto();
    }

    /**
     * Obtiene el usuario del alumno que hizo la consulta.
     *
     * @return El usuario autor, o {@code null} si su inscripción está dada de baja (participación anónima).
     */
    public Usuario getUsuario() {
        return inscripcion.esInactivo() ? null : inscripcion.getAlumno().getUsuario();
    }

    /**
     * Obtiene el usuario de quien escribió el último mensaje de la consulta.
     *
     * @return El docente de la última respuesta o, si no tiene, el alumno autor.
     */
    public Usuario getUsuarioUltimoMensaje() {
        List<RespuestaForo> respuestas = getRespuestasVigentes();
        return respuestas.isEmpty() ? getUsuario() : respuestas.get(respuestas.size() - 1).getUsuario();
    }

    /**
     * Obtiene el comienzo del texto de la consulta, para mostrarla en los listados.
     *
     * @return El texto, recortado con puntos suspensivos si es largo.
     */
    public String getResumen() {
        String limpio = texto.replaceAll("\\s+", " ").trim();
        return limpio.length() <= LONGITUD_RESUMEN ? limpio : limpio.substring(0, LONGITUD_RESUMEN).trim() + "…";
    }

    /**
     * Indica si la consulta todavía no tiene respuestas vigentes.
     *
     * @return {@code true} si no fue respondida.
     */
    public boolean estaSinResponder() {
        return getRespuestasVigentes().isEmpty();
    }

    /**
     * Obtiene la fecha del último mensaje de la consulta: la de su última respuesta o, si no tiene, la de la propia
     * consulta.
     *
     * @return La fecha y hora del último mensaje.
     */
    public LocalDateTime getFechaUltimoMensaje() {
        List<RespuestaForo> respuestas = getRespuestasVigentes();
        return respuestas.isEmpty() ? fechaCreacion : respuestas.get(respuestas.size() - 1).getFechaCreacion();
    }

    /**
     * Obtiene el nombre de quien escribió el último mensaje de la consulta.
     *
     * @return El nombre completo del docente de la última respuesta o, si no tiene, del alumno autor.
     */
    public String getAutorUltimoMensaje() {
        List<RespuestaForo> respuestas = getRespuestasVigentes();
        return respuestas.isEmpty() ? getAutor() : respuestas.get(respuestas.size() - 1).getAutor();
    }

    /**
     * Indica si la consulta aún se puede modificar: si no pasó el plazo de edición desde su registro.
     *
     * @param minutos El plazo de edición, en minutos.
     * @return {@code true} si está dentro del plazo.
     */
    public boolean estaDentroDelPlazo(int minutos) {
        return LocalDateTime.now().isBefore(fechaCreacion.plusMinutes(minutos));
    }

    /**
     * Indica si un alumno puede modificar la consulta: es el autor, está vigente y está dentro del plazo de edición.
     *
     * @param alumno  El alumno (puede ser {@code null}).
     * @param minutos El plazo de edición, en minutos.
     * @return {@code true} si puede modificarla.
     */
    public boolean puedeModificar(Alumno alumno, int minutos) {
        return alumno != null && !baja && inscripcion.getAlumno().getIdAlumno() == alumno.getIdAlumno()
                && estaDentroDelPlazo(minutos);
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
     * Devuelve una representación en forma de cadena de la consulta del foro.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "ConsultaForo #" + idConsulta;
    }
}
