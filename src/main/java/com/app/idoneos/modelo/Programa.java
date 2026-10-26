package com.app.idoneos.modelo;

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

/**
 * Programa de un curso, con sus objetivos, carga horaria y bibliografía.
 */
@Entity
@Table(name = "Programa")
@Check(name = "ck_programa_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_programa_objetivos", constraints = "trim(objetivos) <> ''")
@Check(name = "ck_programa_bibliografia", constraints = "trim(bibliografia) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Programa {

    /** Cantidad máxima de caracteres del nombre. */
    public static final int LONGITUD_NOMBRE = 50;

    /** Cantidad máxima de caracteres de la descripción. */
    public static final int LONGITUD_DESCRIPCION = 150;

    /**
     * Identificador único del programa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPrograma")
    private int idPrograma;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Nombre del programa.
     */
    @Column(name = "nombre", nullable = false, length = LONGITUD_NOMBRE)
    private String nombre;

    /**
     * Descripción breve del programa.
     */
    @Column(name = "descripcion", nullable = true, length = LONGITUD_DESCRIPCION)
    private String descripcion;

    /**
     * Objetivos del programa.
     */
    @Column(name = "objetivos", nullable = false, columnDefinition = "TEXT")
    private String objetivos;

    /**
     * Carga horaria total del programa, en horas. Es opcional.
     */
    @Column(name = "cargaHorariaTotal", nullable = true)
    private Integer cargaHorariaTotal;

    /**
     * Bibliografía del programa.
     */
    @Column(name = "bibliografia", nullable = false, columnDefinition = "TEXT")
    private String bibliografia;

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
     * Conjunto de cohortes asociados.
     */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<Cohorte> cohortes = new HashSet<>();

    /**
     * Conjunto de unidadesCronograma asociados.
     */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<UnidadCronograma> unidadesCronograma = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "programaPorDefecto", cascade = CascadeType.ALL)
    private Set<DocenteCurso> participacionesDocente = new HashSet<>();

    /**
     * Constructor para crear el programa con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param nombre nombre del programa.
     * @param objetivos objetivos del programa.
     * @param bibliografia bibliografía del programa.
     */
    public Programa(Curso curso, String nombre, String objetivos, String bibliografia) {
        this.curso = curso;
        this.nombre = nombre;
        this.objetivos = objetivos;
        this.bibliografia = bibliografia;
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
     * Devuelve una representación en forma de cadena del programa.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Indica si el programa es el vigente de su curso: el activo de fecha de creación más reciente.
     *
     * @return {@code true} si es el programa vigente.
     */
    public boolean esVigente() {
        Programa vigente = curso.getProgramaVigente();
        return !baja && vigente != null && vigente.getIdPrograma() == idPrograma;
    }

    /**
     * Obtiene el estado del programa dentro de su curso.
     *
     * @return "Vigente", "Anterior" (activo pero reemplazado por uno más nuevo) o "Dado de baja".
     */
    public String getEstado() {
        if (baja) {
            return "Dado de baja";
        }
        return esVigente() ? "Vigente" : "Anterior";
    }

    /**
     * Obtiene el comienzo de los objetivos en una sola línea, para mostrarlos en los listados.
     *
     * @return Los objetivos, recortados con puntos suspensivos si son largos.
     */
    public String getResumenObjetivos() {
        return resumir(objetivos);
    }

    /**
     * Obtiene el comienzo de la bibliografía en una sola línea, para mostrarla en los listados.
     *
     * @return La bibliografía, recortada con puntos suspensivos si es larga.
     */
    public String getResumenBibliografia() {
        return resumir(bibliografia);
    }

    private String resumir(String texto) {
        String limpio = texto == null ? "" : texto.replaceAll("\\s+", " ").trim();
        return limpio.length() <= 90 ? limpio : limpio.substring(0, 90).trim() + "…";
    }

    /**
     * Cuenta las cohortes activas del programa.
     *
     * @return La cantidad de cohortes que no están dadas de baja.
     */
    public int getCantidadCohortesActivas() {
        return (int) cohortes.stream().filter(cohorte -> !cohorte.getBaja()).count();
    }

    /**
     * Cuenta las inscripciones activas de todas las cohortes del programa.
     *
     * @return La cantidad de inscripciones que no están dadas de baja.
     */
    public int getCantidadInscripcionesActivas() {
        return cohortes.stream().mapToInt(Cohorte::getCantidadInscriptos).sum();
    }

    /**
     * Obtiene las cohortes activas del programa, de la más reciente a la más antigua.
     *
     * @return Las cohortes vigentes.
     */
    public List<Cohorte> getCohortesActivas() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja())
                .sorted(Comparator.comparingInt(Cohorte::getIdCohorte).reversed()).toList();
    }

    /**
     * Indica si el programa tiene cohortes que todavía no terminaron (próximas, abiertas o en dictado). Mientras las tenga,
     * cada unidad de su cronograma debe conservar al menos una autoevaluación visible: es lo que permite a los alumnos
     * completarla.
     *
     * @return {@code true} si alguna cohorte activa no está finalizada.
     */
    public boolean tieneCohortesEnCurso() {
        return cohortes.stream().anyMatch(Cohorte::estaEnCurso);
    }

    /**
     * Obtiene las unidades del cronograma que no tienen ninguna autoevaluación visible (vigente y no oculta).
     *
     * @return Las unidades sin autoevaluación visible, en el orden del cronograma.
     */
    public List<Unidad> getUnidadesSinAutoevaluacion() {
        return getCronogramaOrdenado().stream().map(UnidadCronograma::getUnidad)
                .filter(unidad -> unidad.getAutoevaluacionesPublicadas().isEmpty()).toList();
    }

    /**
     * Calcula la duración total del cronograma del programa, sumando las semanas de cada unidad vigente.
     *
     * @return La duración total en semanas.
     */
    public int getDuracionTotalSemanas() {
        return unidadesCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .mapToInt(UnidadCronograma::getSemanasDuracion).sum();
    }

    /**
     * Cuenta las unidades del cronograma con contenido publicado: las que tienen al menos un material (vigente y no oculto)
     * y al menos una autoevaluación visible.
     *
     * @return La cantidad de unidades con material publicado y autoevaluación.
     */
    public int getCantidadUnidadesConMaterialPublicado() {
        return cantidadUnidadesConMaterialPublicadoSin(java.util.Set.of(), java.util.Set.of());
    }

    /**
     * Obtiene las unidades que hoy cuentan (con material publicado y autoevaluación) y dejarían de contar si se quitaran
     * ciertos materiales: las que se quedarían sin ningún material publicado.
     *
     * @param materialesQuitados Los identificadores de los materiales que dejarían de estar publicados.
     * @return Las unidades que dejarían de contar, en el orden del cronograma.
     */
    public java.util.List<Unidad> unidadesQueDejanDeContarSin(java.util.Collection<Integer> materialesQuitados) {
        return unidadesCronograma.stream().filter(cronograma -> !cronograma.getBaja()).map(UnidadCronograma::getUnidad)
                .filter(unidad -> !unidad.getAutoevaluacionesPublicadas().isEmpty())
                .filter(unidad -> unidad.getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .filter(unidad -> unidad.getMateriales().stream().noneMatch(m -> !m.getBaja() && !m.getOculto()
                        && !materialesQuitados.contains(m.getIdMaterial())))
                .toList();
    }

    /**
     * Cuenta las unidades con material publicado y autoevaluación que quedarían si se quitaran ciertos materiales (por
     * ocultarlos o darlos de baja) y ciertas unidades del cronograma.
     *
     * @param materialesQuitados Los identificadores de los materiales que dejarían de estar publicados.
     * @param unidadesQuitadas   Los identificadores de las unidades que dejarían de formar parte del cronograma.
     * @return La cantidad de unidades que conservarían material publicado y autoevaluación.
     */
    public int cantidadUnidadesConMaterialPublicadoSin(java.util.Collection<Integer> materialesQuitados,
            java.util.Collection<Integer> unidadesQuitadas) {
        return (int) unidadesCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .map(UnidadCronograma::getUnidad).filter(unidad -> !unidadesQuitadas.contains(unidad.getIdUnidad()))
                .filter(unidad -> !unidad.getAutoevaluacionesPublicadas().isEmpty())
                .filter(unidad -> unidad.getMateriales().stream().anyMatch(material -> !material.getBaja()
                        && !material.getOculto() && !materialesQuitados.contains(material.getIdMaterial())))
                .count();
    }

    /**
     * Cuenta las unidades vigentes del cronograma.
     *
     * @return La cantidad de unidades del cronograma.
     */
    public int getCantidadUnidades() {
        return (int) unidadesCronograma.stream().filter(cronograma -> !cronograma.getBaja()).count();
    }

    /**
     * Obtiene las unidades vigentes del cronograma, en orden de dictado.
     *
     * @return Las unidades del cronograma ordenadas por su número de orden.
     */
    public List<UnidadCronograma> getCronogramaOrdenado() {
        return unidadesCronograma.stream().filter(cronograma -> !cronograma.getBaja() && !cronograma.getUnidad().getBaja())
                .sorted(Comparator.comparingInt(UnidadCronograma::getNumeroOrden)).toList();
    }

    /**
     * Calcula la última semana del programa en la que se dicta una unidad (suma de las semanas de las unidades
     * anteriores y la propia).
     *
     * @param unidadCronograma La unidad del cronograma.
     * @return El número de semana en que termina la unidad.
     */
    public int getSemanaHasta(UnidadCronograma unidadCronograma) {
        return getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getNumeroOrden() <= unidadCronograma.getNumeroOrden())
                .mapToInt(UnidadCronograma::getSemanasDuracion).sum();
    }

    /**
     * Calcula la primera semana del programa en la que se dicta una unidad.
     *
     * @param unidadCronograma La unidad del cronograma.
     * @return El número de semana en que empieza la unidad.
     */
    public int getSemanaDesde(UnidadCronograma unidadCronograma) {
        return getSemanaHasta(unidadCronograma) - unidadCronograma.getSemanasDuracion() + 1;
    }

    /**
     * Elige la cohorte más relevante del programa: la que está en dictado, si no la que tiene la inscripción
     * abierta y, si no, la más reciente.
     *
     * @return La cohorte elegida, o {@code null} si el programa no tiene cohortes vigentes.
     */
    public Cohorte getCohorteDestacada() {
        List<Cohorte> vigentes = cohortes.stream().filter(cohorte -> !cohorte.getBaja()).toList();
        for (String estado : List.of("En dictado", "Abierta")) {
            Cohorte elegida = vigentes.stream().filter(cohorte -> estado.equals(cohorte.getEstado()))
                    .max(Comparator.comparingInt(Cohorte::getIdCohorte)).orElse(null);
            if (elegida != null) {
                return elegida;
            }
        }
        return vigentes.stream().max(Comparator.comparingInt(Cohorte::getIdCohorte)).orElse(null);
    }
}
