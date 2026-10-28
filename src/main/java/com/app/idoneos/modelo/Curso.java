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
@Check(name = "ck_curso_carga_horaria", constraints = "carga_horaria_minutos > 0")
@Check(name = "ck_curso_duracion_maxima", constraints = "duracion_maxima_dias > 0")
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
     * Relación con Nivel.
     */
    @ManyToOne
    @JoinColumn(name = "idNivel", nullable = false)
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
     * Carga horaria total del curso, en minutos: el tiempo de trabajo del docente que reconoce la empresa y que el
     * docente reparte entre los temas. La define el administrador.
     */
    @Column(name = "cargaHorariaMinutos", nullable = false)
    private int cargaHorariaMinutos;

    /**
     * Duración máxima del curso, en días: el tiempo de calendario que tiene el alumno para completarlo. La suma de los
     * temas de cualquier cronograma del curso no puede superarla. La define el administrador.
     */
    @Column(name = "duracionMaximaDias", nullable = false)
    private int duracionMaximaDias;

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
     * @param nivel relación con Nivel.
     * @param categoria relación con Categoria.
     * @param nombre nombre del curso.
     * @param precio precio del curso.
     * @param cargaHorariaMinutos carga horaria total del curso, en minutos.
     * @param duracionMaximaDias duración máxima del curso, en días.
     */
    public Curso(Nivel nivel, Categoria categoria, String nombre, float precio, int cargaHorariaMinutos,
            int duracionMaximaDias) {
        this.nivel = nivel;
        this.categoria = categoria;
        this.nombre = nombre;
        this.precio = precio;
        this.cargaHorariaMinutos = cargaHorariaMinutos;
        this.duracionMaximaDias = duracionMaximaDias;
    }

    /**
     * Suma la carga horaria de los temas vigentes del curso.
     *
     * @return Los minutos repartidos entre los temas.
     */
    public int getCargaHorariaRepartidaMinutos() {
        return temas.stream().filter(tema -> !tema.getBaja()).mapToInt(Tema::getCargaHorariaMinutos).sum();
    }

    /**
     * Indica si la carga horaria del curso está repartida por completo entre sus temas: tiene temas vigentes y la suma de
     * sus minutos es igual a la carga horaria del curso.
     *
     * @return {@code true} si todo lo que paga la empresa está asignado a algún tema.
     */
    public boolean estaCargaHorariaCompleta() {
        return temas.stream().anyMatch(tema -> !tema.getBaja()) && getCargaHorariaRepartidaMinutos() == cargaHorariaMinutos;
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
     * @return Las cohortes con fechas de dictado de todos los programas activos.
     */
    public List<Cohorte> getCohortesConFechasDeDictado() {
        return getProgramasActivos().stream().flatMap(programa -> programa.getCohortesActivas().stream())
                .filter(cohorte -> cohorte.getFechaInicioDictado() != null || cohorte.getFechaFinDictado() != null).toList();
    }

    /**
     * Obtiene las cohortes vigentes del curso que no tienen fechas de dictado (las de los cursos grabados).
     *
     * @return Las cohortes sin fechas de dictado de todos los programas activos.
     */
    public List<Cohorte> getCohortesSinFechasDeDictado() {
        return getProgramasActivos().stream().flatMap(programa -> programa.getCohortesActivas().stream())
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
