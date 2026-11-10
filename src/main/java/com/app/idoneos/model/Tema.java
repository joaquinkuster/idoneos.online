package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un tema de un curso, con su contenido. */
@Entity
@Table(name = "Tema", uniqueConstraints = { @UniqueConstraint(columnNames = {"idCurso", "titulo"}) })
@Check(name = "ck_tema_titulo", constraints = "trim(titulo) <> ''")
@Check(name = "ck_tema_contenido", constraints = "contenido is null or trim(contenido) <> ''")
@Check(name = "ck_tema_descripcion", constraints = "descripcion is null or trim(descripcion) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Tema {

    /** Cantidad máxima de caracteres del título. */
    public static final int LONGITUD_TITULO = 50;

    /** Cantidad máxima de caracteres de la descripción. */
    public static final int LONGITUD_DESCRIPCION = 150;

    /** Identificador único del tema. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTema")
    private int idTema;

    /** Curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Título del tema. */
    @Column(name = "titulo", nullable = false, length = LONGITUD_TITULO)
    private String titulo;

    /** Descripción breve del tema. */
    @Column(name = "descripcion", nullable = true, length = LONGITUD_DESCRIPCION)
    private String descripcion;

    /** Contenido del tema. */
    @Column(name = "contenido", nullable = true, columnDefinition = "TEXT")
    private String contenido;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Conjunto de clases en vivo que dictan el tema. */
    @OneToMany(mappedBy = "tema")
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /** Progresos que referencian a este registro. */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<Progreso> progresos = new HashSet<>();

    /** Conjunto de recursos del tema (su material). */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<TemaRecurso> temasRecurso = new HashSet<>();

    /** Terminos glosario que referencian a este registro. */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<TerminoGlosario> terminosGlosario = new HashSet<>();

    /** Consultas foro que referencian a este registro. */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<ConsultaForo> consultasForo = new HashSet<>();

    /** Temas cronograma que referencian a este registro. */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<TemaCronograma> temasCronograma = new HashSet<>();

    /** Pooles que referencian a este registro. */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<Pool> pooles = new HashSet<>();

    /** Autoevaluaciones que referencian a este registro. */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL)
    private Set<Autoevaluacion> autoevaluaciones = new HashSet<>();

    /** Crea el tema con los datos básicos. */
    public Tema(Curso curso, String titulo, String contenido) {
        this.curso = curso;
        this.titulo = titulo;
        this.contenido = contenido;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del tema. */
    @Override
    public String toString() {
        return titulo;
    }

    /** Obtiene el material publicado del tema (vigente y no oculto). */
    public List<TemaRecurso> getMaterialesPublicados() {
        return temasRecurso.stream().filter(TemaRecurso::estaPublicado)
                .sorted(Comparator.comparingInt(TemaRecurso::getNumeroOrden).thenComparingInt(TemaRecurso::getIdTemaRecurso))
                .toList();
    }

    /** Obtiene el material vigente del tema (no dado de baja), incluido el oculto: es el que ve el docente. */
    public List<TemaRecurso> getMaterialesVigentes() {
        return temasRecurso.stream().filter(material -> !material.getBaja() && !material.getRecurso().getBaja())
                .sorted(Comparator.comparingInt(TemaRecurso::getNumeroOrden).thenComparingInt(TemaRecurso::getIdTemaRecurso))
                .toList();
    }

    /** Obtiene todos los recursos del tema, también los dados de baja. */
    public Set<TemaRecurso> getMateriales() {
        return temasRecurso;
    }

    /** Obtiene el comienzo del contenido en una sola línea, para mostrarlo en los listados. */
    public String getResumenContenido() {
        String limpio = contenido == null ? "" : contenido.replaceAll("\\s+", " ").trim();
        return limpio.length() <= 90 ? limpio : limpio.substring(0, 90).trim() + "…";
    }

    /** Obtiene los programas activos en cuyo cronograma está incluida el tema. */
    public List<Programa> getProgramasQueLaIncluyen() {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .map(TemaCronograma::getPrograma).filter(programa -> !programa.getBaja()).distinct()
                .sorted(Comparator.comparingInt(Programa::getIdPrograma)).toList();
    }

    /** Obtiene los identificadores de los programas que incluyen el tema. */
    public String getIdsProgramasQueLaIncluyen() {
        return String.join(",", getProgramasQueLaIncluyen().stream().map(p -> String.valueOf(p.getIdPrograma())).toList());
    }

    /** Indica si el tema está incluida en el cronograma de un programa. */
    public boolean perteneceAlCronograma(Programa programa) {
        return temasCronograma.stream()
                .anyMatch(cronograma -> cronograma.getPrograma().getIdPrograma() == programa.getIdPrograma());
    }

    /** Indica si algún programa que incluye el tema tiene inscripciones activas. */
    public boolean tieneInscripcionesActivas() {
        return getProgramasQueLaIncluyen().stream().anyMatch(programa -> programa.getCantidadInscripcionesActivas() > 0);
    }

    /** Obtiene los términos vigentes del glosario del tema. */
    public List<TerminoGlosario> getTerminosGlosarioOrdenados() {
        return terminosGlosario.stream().filter(termino -> !termino.getBaja())
                .sorted(Comparator.comparing(TerminoGlosario::getTermino, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    /** Obtiene la fecha de la última actualización del glosario del tema. */
    public LocalDateTime getUltimaActualizacionGlosario() {
        return terminosGlosario.stream().filter(termino -> !termino.getBaja())
                .map(TerminoGlosario::getUltimaActualizacion).max(Comparator.naturalOrder()).orElse(null);
    }

    /** Obtiene las autoevaluaciones vigentes del tema (no dadas de baja), también las ocultas. */
    public List<Autoevaluacion> getAutoevaluacionesVigentes() {
        return autoevaluaciones.stream().filter(autoevaluacion -> !autoevaluacion.getBaja())
                .sorted(Comparator.comparingInt(Autoevaluacion::getIdAutoevaluacion)).toList();
    }

    /** Obtiene las autoevaluaciones publicadas del tema (vigentes y no ocultas). */
    public List<Autoevaluacion> getAutoevaluacionesPublicadas() {
        return autoevaluaciones.stream().filter(autoevaluacion -> !autoevaluacion.getBaja() && !autoevaluacion.getOculto())
                .sorted(Comparator.comparingInt(Autoevaluacion::getIdAutoevaluacion)).toList();
    }

    /** Indica si un alumno aprobó el tema: el tema tiene al menos una autoevaluación publicada y el alumno aprobó todas las que tiene publicadas (vigentes y no ocultas). */
    public boolean estaAprobadaPor(Inscripcion inscripcion) {
        List<Autoevaluacion> publicadas = getAutoevaluacionesPublicadas();
        return !publicadas.isEmpty() && publicadas.stream().allMatch(autoevaluacion -> autoevaluacion.estaAprobadaPor(inscripcion));
    }

    /** Indica si algún programa que incluye el tema tiene cohortes que todavía no terminaron. */
    public boolean tieneCohortesActivas() {
        return getProgramasQueLaIncluyen().stream().anyMatch(Programa::tieneCohortesActivas);
    }

    /** Indica si el tema es el último del cronograma de un programa. */
    public boolean esUltimaDelPrograma(Programa programa) {
        List<TemaCronograma> cronograma = programa.getCronogramaOrdenado();
        return !cronograma.isEmpty() && cronograma.get(cronograma.size() - 1).getTema().getIdTema() == idTema;
    }

    /** Obtiene las consultas vigentes del foro del tema. */
    public List<ConsultaForo> getConsultasForoOrdenadas() {
        return consultasForo.stream().filter(consulta -> !consulta.getBaja())
                .sorted(Comparator.comparing(ConsultaForo::getFechaUltimoMensaje).reversed()
                        .thenComparing(Comparator.comparingInt(ConsultaForo::getIdConsulta).reversed()))
                .toList();
    }

    /** Cuenta las consultas vigentes del foro del tema que todavía no tienen respuestas. */
    public int getCantidadConsultasSinResponder() {
        return (int) consultasForo.stream().filter(consulta -> !consulta.getBaja() && consulta.estaSinResponder())
                .count();
    }

    /** Obtiene la fecha del último mensaje del foro del tema. */
    public LocalDateTime getUltimaActividadForo() {
        return getConsultasForoOrdenadas().stream().map(ConsultaForo::getFechaUltimoMensaje).findFirst().orElse(null);
    }

    /** Cuenta las consultas vigentes registradas en el foro del tema. */
    public int getCantidadConsultasForo() {
        return (int) consultasForo.stream().filter(consulta -> !consulta.getBaja()).count();
    }
}
