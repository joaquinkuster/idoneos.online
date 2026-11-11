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

/** Entidad que representa la oferta educativa principal de la plataforma. */
@Entity
@Table(name = "Curso")
@Check(name = "ck_curso_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_curso_precio", constraints = "precio >= 0 and precio <= 10000000")
@Check(name = "ck_curso_carga_horaria", constraints = "carga_horaria_minutos is null or (carga_horaria_minutos > 0 and carga_horaria_minutos <= 60000 and carga_horaria_minutos <= cast(duracion_dias as bigint) * 1440)")
@Check(name = "ck_curso_duracion", constraints = "duracion_dias >= 1 and duracion_dias <= 700")
@Check(name = "ck_curso_objetivos", constraints = "objetivos is null or trim(objetivos) <> ''")
@Check(name = "ck_curso_descripcion", constraints = "descripcion is null or trim(descripcion) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Curso {

    /** Precio máximo permitido para un curso, en pesos argentinos (ARS). */
    public static final float PRECIO_MAXIMO = 10_000_000f;

    /** Carga horaria máxima permitida para un curso, en minutos (1.000 horas). */
    public static final int CARGA_HORARIA_MAXIMA_MINUTOS = 60_000;

    /** Identificador único del curso. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCurso")
    private int idCurso;

    /** Nivel de este registro. */
    @ManyToOne
    @JoinColumn(name = "idNivel", nullable = true)
    private Nivel nivel;

    /** Categoria de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    /** Nombre del curso. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Descripción breve del curso. */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /** Precio del curso. */
    @Column(name = "precio", nullable = false)
    private float precio;

    /** Objetivos del curso. */
    @Column(name = "objetivos", nullable = true, columnDefinition = "TEXT")
    private String objetivos;

    /** Carga horaria del curso, en minutos: el total de horas que se informa en el certificado. */
    @Column(name = "cargaHorariaMinutos", nullable = true)
    private Integer cargaHorariaMinutos;

    /** Duración del curso, en días: el tiempo de calendario que el docente asigna entre los temas del programa. */
    @Column(name = "duracionDias", nullable = false)
    private int duracionDias;

    /** Ruta de la imagen de portada del curso. */
    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    /** Indica si el curso emite certificado de aprobación. */
    @Column(name = "emiteCertificado", nullable = false)
    private Boolean emiteCertificado = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Reportes que referencian a este registro. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Reporte> reportes = new HashSet<>();

    /** Temas que referencian a este registro. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Tema> temas = new HashSet<>();

    /** Programas que referencian a este registro. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Programa> programas = new HashSet<>();

    /** Conjunto de cohortes del curso, tengan o no programa. */
    @OneToMany(mappedBy = "curso")
    private Set<Cohorte> cohortes = new HashSet<>();

    /** Participaciones docente que referencian a este registro. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<DocenteCurso> participacionesDocente = new HashSet<>();

    /** Curso modalidades que referencian a este registro. */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<CursoModalidad> cursoModalidades = new HashSet<>();

    /** Crea el curso con los datos básicos. */
    public Curso(Nivel nivel, Categoria categoria, String nombre, float precio, Integer cargaHorariaMinutos, int duracionDias) {
        this.nivel = nivel;
        this.categoria = categoria;
        this.nombre = nombre;
        this.precio = precio;
        this.cargaHorariaMinutos = cargaHorariaMinutos;
        this.duracionDias = duracionDias;
    }

    /** Indica qué datos académicos le faltan al curso: los que completa el docente al diseñarlo. */
    public List<String> getFaltantesDeDatosAcademicos() {
        List<String> faltantes = new java.util.ArrayList<>();
        if (nivel == null) faltantes.add("nivel");
        if (objetivos == null || objetivos.isBlank()) faltantes.add("objetivos");
        if (Boolean.TRUE.equals(emiteCertificado) && (cargaHorariaMinutos == null || cargaHorariaMinutos <= 0)) {
            faltantes.add("carga horaria");
        }
        return faltantes;
    }

    /** Indica si el docente ya completó los datos académicos del curso. */
    public boolean tieneDatosAcademicosCompletos() {
        return getFaltantesDeDatosAcademicos().isEmpty();
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del curso. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Obtiene las participaciones vigentes (no dadas de baja) que conforman el equipo docente del curso. */
    public List<DocenteCurso> getEquipoDocente() {
        return participacionesDocente.stream().filter(participacion -> !participacion.getBaja())
                .sorted(Comparator.comparingInt(DocenteCurso::getIdDocenteCurso)).toList();
    }

    /** Obtiene el docente titular vigente del curso. */
    public Docente getDocenteTitular() {
        return getEquipoDocente().stream().filter(DocenteCurso::getEsTitular)
                .map(DocenteCurso::getDocente).findFirst().orElse(null);
    }

    /** Indica si un docente forma parte del equipo docente vigente del curso (como titular o ayudante). */
    public boolean tieneDocente(Docente docente) {
        return docente != null && tieneDocente(docente.getIdDocente());
    }

    /** Indica si un docente forma parte del equipo docente vigente del curso. */
    public boolean tieneDocente(int idDocente) {
        return getEquipoDocente().stream()
                .anyMatch(participacion -> participacion.getDocente().getIdDocente() == idDocente);
    }

    /** Obtiene los docentes ayudantes vigentes del curso. */
    public List<Docente> getDocentesAyudantes() {
        return getEquipoDocente().stream().filter(participacion -> !participacion.getEsTitular())
                .map(DocenteCurso::getDocente).toList();
    }

    /** Obtiene las modalidades de dictado en las que se ofrece el curso. */
    public List<Modalidad> getModalidades() {
        return cursoModalidades.stream().map(CursoModalidad::getModalidad).toList();
    }

    /** Verifica si el curso se ofrece en una modalidad. */
    public boolean incluyeModalidad(String nombreModalidad) {
        return getModalidades().stream().anyMatch(modalidad -> nombreModalidad.equalsIgnoreCase(modalidad.getNombre()));
    }

    /** Obtiene el material publicado (vigente y visible) del curso que pertenece a una modalidad. */
    public List<TemaRecurso> getMaterialesPublicadosDeModalidad(String nombreModalidad) {
        return temas.stream().filter(tema -> !tema.getBaja()).flatMap(tema -> tema.getMateriales().stream())
                .filter(material -> material.estaPublicado() && nombreModalidad.equals(material.getModalidadAsociada()))
                .toList();
    }

    /** Indica si el curso tiene alguna cohorte publicada en el catálogo (desde la inscripción próxima en adelante). */
    public boolean tieneCohortesPublicadas() {
        return cohortes.stream().anyMatch(Cohorte::estaPublicada);
    }

    /** Obtiene las cohortes del curso que no están dadas de baja ni canceladas (tengan o no programa). */
    public List<Cohorte> getCohortesDeTrabajo() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja() && !cohorte.esCancelada())
                .sorted(java.util.Comparator.comparingInt(Cohorte::getIdCohorte).reversed()).toList();
    }

    /** Obtiene las cohortes vigentes del curso que tienen fechas de dictado (las de los cursos en vivo). */
    public List<Cohorte> getCohortesConFechasDeDictado() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja())
                .filter(cohorte -> cohorte.getFechaInicioDictado() != null || cohorte.getFechaFinDictado() != null).toList();
    }

    /** Obtiene las cohortes vigentes del curso que no tienen fechas de dictado (las de los cursos grabados). */
    public List<Cohorte> getCohortesSinFechasDeDictado() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja())
                .filter(cohorte -> cohorte.getFechaInicioDictado() == null && cohorte.getFechaFinDictado() == null).toList();
    }

    /** Obtiene el programa vigente del curso: el más reciente que no está dado de baja. */
    public Programa getProgramaVigente() {
        return programas.stream().filter(programa -> !programa.getBaja())
                .max(Comparator.comparingInt(Programa::getIdPrograma)).orElse(null);
    }

    /** Obtiene los programas activos del curso. */
    public List<Programa> getProgramasActivos() {
        return programas.stream().filter(programa -> !programa.getBaja())
                .sorted(Comparator.comparingInt(Programa::getIdPrograma).reversed()).toList();
    }

    /** Obtiene la URL pública de la imagen del curso. */
    public String getUrlImagen() {
        if (imagen == null || imagen.isBlank()) {
            return null;
        }
        long version = ultimaModificacion == null ? 0 : ultimaModificacion.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        return "/uploads/portadas/" + imagen + "?v=" + version;
    }
}
