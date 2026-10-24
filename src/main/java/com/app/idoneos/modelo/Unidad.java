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
 * Unidad temática de un curso, con su contenido.
 */
@Entity
@Table(name = "Unidad", uniqueConstraints = { @UniqueConstraint(columnNames = {"idCurso", "titulo"}) })
@Getter
@Setter
@NoArgsConstructor
public class Unidad {

    /** Cantidad máxima de caracteres del título. */
    public static final int LONGITUD_TITULO = 50;

    /** Cantidad máxima de caracteres de la descripción. */
    public static final int LONGITUD_DESCRIPCION = 150;

    /**
     * Identificador único de la unidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUnidad")
    private int idUnidad;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Título de la unidad.
     */
    @Column(name = "titulo", nullable = false, length = LONGITUD_TITULO)
    private String titulo;

    /**
     * Descripción breve de la unidad.
     */
    @Column(name = "descripcion", nullable = true, length = LONGITUD_DESCRIPCION)
    private String descripcion;

    /**
     * Contenido de la unidad.
     */
    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

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
     * Conjunto de progresos asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Progreso> progresos = new HashSet<>();

    /**
     * Conjunto de materiales asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Material> materiales = new HashSet<>();

    /**
     * Conjunto de terminosGlosario asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<TerminoGlosario> terminosGlosario = new HashSet<>();

    /**
     * Conjunto de consultasForo asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<ConsultaForo> consultasForo = new HashSet<>();

    /**
     * Conjunto de unidadesCronograma asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<UnidadCronograma> unidadesCronograma = new HashSet<>();

    /**
     * Conjunto de pooles asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Pool> pooles = new HashSet<>();

    /**
     * Conjunto de autoevaluaciones asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Autoevaluacion> autoevaluaciones = new HashSet<>();

    /**
     * Constructor para crear la unidad con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param titulo título de la unidad.
     * @param contenido contenido de la unidad.
     */
    public Unidad(Curso curso, String titulo, String contenido) {
        this.curso = curso;
        this.titulo = titulo;
        this.contenido = contenido;
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
     * Devuelve una representación en forma de cadena de la unidad.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }

    /**
     * Obtiene el material publicado de la unidad (vigente y no oculto).
     *
     * @return El material publicado, en el orden en que se cargó.
     */
    public List<Material> getMaterialesPublicados() {
        return materiales.stream().filter(material -> !material.getBaja() && !material.getOculto())
                .sorted(Comparator.comparingInt(Material::getIdMaterial)).toList();
    }

    /**
     * Obtiene el material vigente de la unidad (no dado de baja), incluido el oculto: es el que ve el docente.
     *
     * @return El material vigente, en el orden en que se cargó.
     */
    public List<Material> getMaterialesVigentes() {
        return materiales.stream().filter(material -> !material.getBaja())
                .sorted(Comparator.comparingInt(Material::getIdMaterial)).toList();
    }

    /**
     * Obtiene el comienzo del contenido en una sola línea, para mostrarlo en los listados.
     *
     * @return El contenido, recortado con puntos suspensivos si es largo.
     */
    public String getResumenContenido() {
        String limpio = contenido == null ? "" : contenido.replaceAll("\\s+", " ").trim();
        return limpio.length() <= 90 ? limpio : limpio.substring(0, 90).trim() + "…";
    }

    /**
     * Obtiene los programas activos en cuyo cronograma está incluida la unidad.
     *
     * @return Los programas, del más antiguo al más reciente.
     */
    public List<Programa> getProgramasQueLaIncluyen() {
        return unidadesCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .map(UnidadCronograma::getPrograma).filter(programa -> !programa.getBaja()).distinct()
                .sorted(Comparator.comparingInt(Programa::getIdPrograma)).toList();
    }

    /**
     * Obtiene los identificadores de los programas que incluyen la unidad, separados por coma (para los formularios).
     *
     * @return Por ejemplo "1,3", o una cadena vacía si ningún programa la incluye.
     */
    public String getIdsProgramasQueLaIncluyen() {
        return String.join(",", getProgramasQueLaIncluyen().stream().map(p -> String.valueOf(p.getIdPrograma())).toList());
    }

    /**
     * Indica si la unidad está incluida en el cronograma de un programa.
     *
     * @param programa El programa.
     * @return {@code true} si el programa la incluye.
     */
    public boolean perteneceAlCronograma(Programa programa) {
        return unidadesCronograma.stream()
                .anyMatch(cronograma -> cronograma.getPrograma().getIdPrograma() == programa.getIdPrograma());
    }

    /**
     * Indica si algún programa que incluye la unidad tiene inscripciones activas: en ese caso la unidad es parte de lo
     * que el alumno vio y pagó, y no puede modificarse.
     *
     * @return {@code true} si hay inscripciones activas asociadas.
     */
    public boolean tieneInscripcionesActivas() {
        return getProgramasQueLaIncluyen().stream().anyMatch(programa -> programa.getCantidadInscripcionesActivas() > 0);
    }

    /**
     * Obtiene los términos vigentes del glosario de la unidad.
     *
     * @return Los términos, en orden alfabético.
     */
    public List<TerminoGlosario> getTerminosGlosarioOrdenados() {
        return terminosGlosario.stream().filter(termino -> !termino.getBaja())
                .sorted(Comparator.comparing(TerminoGlosario::getTermino, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    /**
     * Obtiene la fecha de la última actualización del glosario de la unidad: la más reciente entre sus términos
     * vigentes.
     *
     * @return La fecha y hora, o {@code null} si el glosario no tiene términos.
     */
    public LocalDateTime getUltimaActualizacionGlosario() {
        return terminosGlosario.stream().filter(termino -> !termino.getBaja())
                .map(TerminoGlosario::getUltimaActualizacion).max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Obtiene las autoevaluaciones vigentes de la unidad (no dadas de baja), también las ocultas: es lo que ve el docente.
     *
     * @return Las autoevaluaciones, en el orden en que se cargaron.
     */
    public List<Autoevaluacion> getAutoevaluacionesVigentes() {
        return autoevaluaciones.stream().filter(autoevaluacion -> !autoevaluacion.getBaja())
                .sorted(Comparator.comparingInt(Autoevaluacion::getIdAutoevaluacion)).toList();
    }

    /**
     * Obtiene las autoevaluaciones publicadas de la unidad (vigentes y no ocultas).
     *
     * @return Las autoevaluaciones, en el orden en que se cargaron.
     */
    public List<Autoevaluacion> getAutoevaluacionesPublicadas() {
        return autoevaluaciones.stream().filter(autoevaluacion -> !autoevaluacion.getBaja() && !autoevaluacion.getOculto())
                .sorted(Comparator.comparingInt(Autoevaluacion::getIdAutoevaluacion)).toList();
    }

    /**
     * Indica si un alumno aprobó la unidad: la unidad tiene al menos una autoevaluación publicada y el alumno aprobó todas
     * las que tiene publicadas (vigentes y no ocultas).
     *
     * @param inscripcion La inscripción del alumno.
     * @return {@code true} si el alumno cumple con todas las autoevaluaciones de la unidad.
     */
    public boolean estaAprobadaPor(Inscripcion inscripcion) {
        List<Autoevaluacion> publicadas = getAutoevaluacionesPublicadas();
        return !publicadas.isEmpty() && publicadas.stream().allMatch(autoevaluacion -> autoevaluacion.estaAprobadaPor(inscripcion));
    }

    /**
     * Indica si algún programa que incluye la unidad tiene cohortes que todavía no terminaron. En ese caso la unidad debe
     * conservar al menos una autoevaluación visible.
     *
     * @return {@code true} si la unidad es cursada (o lo será) por alumnos de alguna cohorte.
     */
    public boolean tieneCohortesEnCurso() {
        return getProgramasQueLaIncluyen().stream().anyMatch(Programa::tieneCohortesEnCurso);
    }

    /**
     * Indica si la unidad es la última del cronograma de un programa.
     *
     * @param programa El programa.
     * @return {@code true} si es la última unidad del cronograma.
     */
    public boolean esUltimaDelPrograma(Programa programa) {
        List<UnidadCronograma> cronograma = programa.getCronogramaOrdenado();
        return !cronograma.isEmpty() && cronograma.get(cronograma.size() - 1).getUnidad().getIdUnidad() == idUnidad;
    }

    /**
     * Obtiene las consultas vigentes del foro de la unidad.
     *
     * @return Las consultas, con el último mensaje más reciente primero.
     */
    public List<ConsultaForo> getConsultasForoOrdenadas() {
        return consultasForo.stream().filter(consulta -> !consulta.getBaja())
                .sorted(Comparator.comparing(ConsultaForo::getFechaUltimoMensaje).reversed()
                        .thenComparing(Comparator.comparingInt(ConsultaForo::getIdConsulta).reversed()))
                .toList();
    }

    /**
     * Cuenta las consultas vigentes del foro de la unidad que todavía no tienen respuestas.
     *
     * @return La cantidad de consultas sin responder.
     */
    public int getCantidadConsultasSinResponder() {
        return (int) consultasForo.stream().filter(consulta -> !consulta.getBaja() && consulta.estaSinResponder())
                .count();
    }

    /**
     * Obtiene la fecha del último mensaje del foro de la unidad.
     *
     * @return La fecha y hora, o {@code null} si el foro no tiene consultas.
     */
    public LocalDateTime getUltimaActividadForo() {
        return getConsultasForoOrdenadas().stream().map(ConsultaForo::getFechaUltimoMensaje).findFirst().orElse(null);
    }

    /**
     * Cuenta las consultas vigentes registradas en el foro de la unidad.
     *
     * @return La cantidad de consultas.
     */
    public int getCantidadConsultasForo() {
        return (int) consultasForo.stream().filter(consulta -> !consulta.getBaja()).count();
    }
}
