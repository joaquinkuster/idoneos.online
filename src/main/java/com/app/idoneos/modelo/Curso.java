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
 * Oferta educativa principal de la plataforma. Se dicta en una o más modalidades y se organiza en programas y temas.
 */
@Entity
@Table(name = "Curso")
@Check(name = "ck_curso_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_curso_precio", constraints = "precio >= 0")
@Check(name = "ck_curso_carga_horaria", constraints = "carga_horaria_minutos is null or carga_horaria_minutos > 0")
@Check(name = "ck_curso_duracion", constraints = "duracion_dias > 0")
@Check(name = "ck_curso_objetivos", constraints = "objetivos is null or trim(objetivos) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Curso {

    /**
     * Precio máximo permitido para un curso, en pesos argentinos (ARS).
     */
    public static final float PRECIO_MAXIMO = 10_000_000f;

    /**
     * Carga horaria máxima permitida para un curso, en minutos (1.000 horas).
     */
    public static final int CARGA_HORARIA_MAXIMA_MINUTOS = 60_000;

    /**
     * Identificador único del curso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCurso")
    private int idCurso;

    /**
     * Relación con Nivel. Lo completa el docente al diseñar el curso, por eso puede estar vacío al registrarlo.
     */
    @ManyToOne
    @JoinColumn(name = "idNivel", nullable = true)
    private Nivel nivel;

    /**
     * Relación con Categoria.
     */
    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    /**
     * Nombre del curso.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Descripción breve del curso.
     */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /**
     * Precio del curso.
     */
    @Column(name = "precio", nullable = false)
    private float precio;

    /**
     * Objetivos del curso. Los completa el docente al diseñar el curso.
     */
    @Column(name = "objetivos", nullable = true, columnDefinition = "TEXT")
    private String objetivos;

    /**
     * Carga horaria del curso, en minutos: el total de horas que se informa en el certificado. No interviene en ningún
     * cálculo. La completa el docente.
     */
    @Column(name = "cargaHorariaMinutos", nullable = true)
    private Integer cargaHorariaMinutos;

    /**
     * Duración del curso, en días: el tiempo de calendario que el docente asigna entre los temas del programa, y el
     * mínimo de acceso que tiene el alumno. La define el administrador.
     */
    @Column(name = "duracionDias", nullable = false)
    private int duracionDias;

    /**
     * Ruta de la imagen de portada del curso. Es opcional.
     */
    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    /**
     * Indica si el curso emite certificado de aprobación.
     */
    @Column(name = "emiteCertificado", nullable = false)
    private Boolean emiteCertificado = false;

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
     * Conjunto de reportes asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Reporte> reportes = new HashSet<>();

    /**
     * Conjunto de temas asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Tema> temas = new HashSet<>();

    /**
     * Conjunto de programas asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Programa> programas = new HashSet<>();

    /**
     * Conjunto de cohortes del curso, tengan o no programa.
     */
    @OneToMany(mappedBy = "curso")
    private Set<Cohorte> cohortes = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<DocenteCurso> participacionesDocente = new HashSet<>();

    /**
     * Conjunto de cursoModalidades asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<CursoModalidad> cursoModalidades = new HashSet<>();

    /**
     * Constructor para crear el curso con los datos básicos.
     *
     * @param nivel relación con Nivel (opcional: lo completa el docente).
     * @param categoria relación con Categoria.
     * @param nombre nombre del curso.
     * @param precio precio del curso.
     * @param cargaHorariaMinutos carga horaria para el certificado, en minutos (opcional: la completa el docente).
     * @param duracionDias duración del curso, en días.
     */
    public Curso(Nivel nivel, Categoria categoria, String nombre, float precio, Integer cargaHorariaMinutos, int duracionDias) {
        this.nivel = nivel;
        this.categoria = categoria;
        this.nombre = nombre;
        this.precio = precio;
        this.cargaHorariaMinutos = cargaHorariaMinutos;
        this.duracionDias = duracionDias;
    }

    /**
     * Indica qué datos académicos le faltan al curso: los que completa el docente al diseñarlo. La carga horaria solo
     * es obligatoria si el curso emite certificado.
     *
     * @return Los nombres de lo que falta ("nivel", "objetivos", "carga horaria"); vacío si está todo.
     */
    public List<String> getFaltantesDeDatosAcademicos() {
        List<String> faltantes = new java.util.ArrayList<>();
        if (nivel == null) faltantes.add("nivel");
        if (objetivos == null || objetivos.isBlank()) faltantes.add("objetivos");
        if (Boolean.TRUE.equals(emiteCertificado) && (cargaHorariaMinutos == null || cargaHorariaMinutos <= 0)) {
            faltantes.add("carga horaria");
        }
        return faltantes;
    }

    /**
     * Indica si el docente ya completó los datos académicos del curso.
     *
     * @return {@code true} si no falta ninguno.
     */
    public boolean tieneDatosAcademicosCompletos() {
        return getFaltantesDeDatosAcademicos().isEmpty();
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
     * Devuelve una representación en forma de cadena del curso.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene las participaciones vigentes (no dadas de baja) que conforman el equipo docente del curso,
     * en el orden en que se incorporaron.
     *
     * @return La lista de participaciones vigentes.
     */
    public List<DocenteCurso> getEquipoDocente() {
        return participacionesDocente.stream().filter(participacion -> !participacion.getBaja())
                .sorted(Comparator.comparingInt(DocenteCurso::getIdDocenteCurso)).toList();
    }

    /**
     * Obtiene el docente titular vigente del curso.
     *
     * @return El docente titular, o {@code null} si no tiene.
     */
    public Docente getDocenteTitular() {
        return getEquipoDocente().stream().filter(DocenteCurso::getEsTitular)
                .map(DocenteCurso::getDocente).findFirst().orElse(null);
    }

    /**
     * Indica si un docente forma parte del equipo docente vigente del curso (como titular o ayudante).
     *
     * @param docente El docente a consultar.
     * @return {@code true} si participa del curso.
     */
    public boolean tieneDocente(Docente docente) {
        return docente != null && tieneDocente(docente.getIdDocente());
    }

    /**
     * Indica si un docente forma parte del equipo docente vigente del curso.
     *
     * @param idDocente El identificador del docente.
     * @return {@code true} si participa del curso.
     */
    public boolean tieneDocente(int idDocente) {
        return getEquipoDocente().stream()
                .anyMatch(participacion -> participacion.getDocente().getIdDocente() == idDocente);
    }

    /**
     * Obtiene los docentes ayudantes vigentes del curso.
     *
     * @return La lista de docentes ayudantes (vacía si no tiene).
     */
    public List<Docente> getDocentesAyudantes() {
        return getEquipoDocente().stream().filter(participacion -> !participacion.getEsTitular())
                .map(DocenteCurso::getDocente).toList();
    }

    /**
     * Obtiene las modalidades de dictado en las que se ofrece el curso.
     *
     * @return La lista de modalidades.
     */
    public List<Modalidad> getModalidades() {
        return cursoModalidades.stream().map(CursoModalidad::getModalidad).toList();
    }

    /**
     * Verifica si el curso se ofrece en una modalidad.
     *
     * @param nombreModalidad El nombre de la modalidad (por ejemplo, "En vivo").
     * @return {@code true} si el curso incluye esa modalidad.
     */
    public boolean incluyeModalidad(String nombreModalidad) {
        return getModalidades().stream().anyMatch(modalidad -> nombreModalidad.equalsIgnoreCase(modalidad.getNombre()));
    }

    /**
     * Obtiene el material publicado (vigente y visible) del curso que pertenece a una modalidad.
     *
     * @param nombreModalidad El nombre de la modalidad (por ejemplo, "Grabada").
     * @return Los materiales publicados de esa modalidad.
     */
    public List<Material> getMaterialesPublicadosDeModalidad(String nombreModalidad) {
        return temas.stream().filter(tema -> !tema.getBaja()).flatMap(tema -> tema.getMateriales().stream())
                .filter(material -> material.estaPublicado() && nombreModalidad.equals(material.getModalidadAsociada()))
                .toList();
    }

    /**
     * Obtiene las cohortes vigentes del curso que tienen fechas de dictado (las de los cursos en vivo).
     *
     * @return Las cohortes vigentes con fechas de dictado.
     */
    public List<Cohorte> getCohortesConFechasDeDictado() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja())
                .filter(cohorte -> cohorte.getFechaInicioDictado() != null || cohorte.getFechaFinDictado() != null).toList();
    }

    /**
     * Obtiene las cohortes vigentes del curso que no tienen fechas de dictado (las de los cursos grabados).
     *
     * @return Las cohortes vigentes sin fechas de dictado.
     */
    public List<Cohorte> getCohortesSinFechasDeDictado() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja())
                .filter(cohorte -> cohorte.getFechaInicioDictado() == null && cohorte.getFechaFinDictado() == null).toList();
    }

    /**
     * Obtiene el programa vigente del curso: el más reciente que no está dado de baja.
     *
     * @return El programa vigente, o {@code null} si el curso no tiene programas vigentes.
     */
    public Programa getProgramaVigente() {
        return programas.stream().filter(programa -> !programa.getBaja())
                .max(Comparator.comparingInt(Programa::getIdPrograma)).orElse(null);
    }

    /**
     * Obtiene los programas activos del curso.
     *
     * @return Los programas que no están dados de baja, el más reciente primero.
     */
    public List<Programa> getProgramasActivos() {
        return programas.stream().filter(programa -> !programa.getBaja())
                .sorted(Comparator.comparingInt(Programa::getIdPrograma).reversed()).toList();
    }

    /**
     * Obtiene la URL pública de la imagen del curso, con la versión de la última modificación para que el
     * navegador no muestre una imagen vieja después de reemplazarla.
     *
     * @return La URL de la imagen, o {@code null} si el curso no tiene imagen.
     */
    public String getUrlImagen() {
        if (imagen == null || imagen.isBlank()) {
            return null;
        }
        long version = ultimaModificacion == null ? 0 : ultimaModificacion.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        return "/uploads/curso" + idCurso + "/" + imagen + "?v=" + version;
    }
}
