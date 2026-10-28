package com.app.idoneos.modelo;

import com.app.idoneos.utilidades.DuracionUtilidad;
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
 * Programa de un curso, con sus objetivos y su cronograma de temas.
 */
@Entity
@Table(name = "Programa")
@Check(name = "ck_programa_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_programa_objetivos", constraints = "trim(objetivos) <> ''")
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
     * Conjunto de temasCronograma asociados.
     */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<TemaCronograma> temasCronograma = new HashSet<>();

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
     */
    public Programa(Curso curso, String nombre, String objetivos) {
        this.curso = curso;
        this.nombre = nombre;
        this.objetivos = objetivos;
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
     * cada tema de su cronograma debe conservar al menos una autoevaluación visible: es lo que permite a los alumnos
     * completarla.
     *
     * @return {@code true} si alguna cohorte activa no está finalizada.
     */
    public boolean tieneCohortesEnCurso() {
        return cohortes.stream().anyMatch(Cohorte::estaEnCurso);
    }

    /**
     * Obtiene los temas del cronograma que no tienen ninguna autoevaluación visible (vigente y no oculta).
     *
     * @return Los temas sin autoevaluación visible, en el orden del cronograma.
     */
    public List<Tema> getTemasSinAutoevaluacion() {
        return getCronogramaOrdenado().stream().map(TemaCronograma::getTema)
                .filter(tema -> tema.getAutoevaluacionesPublicadas().isEmpty()).toList();
    }

    /**
     * Calcula la duración total del cronograma del programa, sumando los días de cada tema vigente.
     *
     * @return La duración total en días.
     */
    public int getDuracionTotalDias() {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .mapToInt(TemaCronograma::getDuracionDias).sum();
    }

    /**
     * Calcula cuántos días representa cada columna al dibujar el cronograma: el mes (30 días) o la semana (7 días) si
     * todos los temas duran un múltiplo exacto de esa unidad, o el día en cualquier otro caso. Así las barras ocupan
     * columnas completas y la grilla no se ensancha de más.
     *
     * @return 30, 7 o 1.
     */
    public int getPasoCronograma() {
        int comun = 0;
        for (TemaCronograma cronograma : getCronogramaOrdenado()) {
            comun = mcd(comun, cronograma.getDuracionDias());
        }
        if (comun > 0 && comun % DuracionUtilidad.DIAS_POR_MES == 0) {
            return DuracionUtilidad.DIAS_POR_MES;
        }
        return comun > 0 && comun % DuracionUtilidad.DIAS_POR_SEMANA == 0 ? DuracionUtilidad.DIAS_POR_SEMANA : 1;
    }

    /**
     * Obtiene el nombre de la unidad que representa cada columna del cronograma.
     *
     * @return "mes", "semana" o "día".
     */
    public String getUnidadCronograma() {
        return switch (getPasoCronograma()) {
            case DuracionUtilidad.DIAS_POR_MES -> "mes";
            case DuracionUtilidad.DIAS_POR_SEMANA -> "semana";
            default -> "día";
        };
    }

    /**
     * Calcula cuántas columnas tiene el cronograma al dibujarlo (una por unidad, ver {@link #getPasoCronograma()}).
     *
     * @return La cantidad de columnas.
     */
    public int getColumnasCronograma() {
        int paso = getPasoCronograma();
        return (getDuracionTotalDias() + paso - 1) / paso;
    }

    private static int mcd(int a, int b) {
        return b == 0 ? a : mcd(b, a % b);
    }

    /**
     * Cuenta los temas del cronograma con contenido publicado: las que tienen al menos un material (vigente y no oculto)
     * y al menos una autoevaluación visible.
     *
     * @return La cantidad de temas con material publicado y autoevaluación.
     */
    public int getCantidadTemasConMaterialPublicado() {
        return cantidadTemasConMaterialPublicadoSin(java.util.Set.of(), java.util.Set.of());
    }

    /**
     * Obtiene los temas que hoy cuentan (con material publicado y autoevaluación) y dejarían de contar si se quitaran
     * ciertos materiales: las que se quedarían sin ningún material publicado.
     *
     * @param materialesQuitados Los identificadores de los materiales que dejarían de estar publicados.
     * @return Los temas que dejarían de contar, en el orden del cronograma.
     */
    public java.util.List<Tema> temasQueDejanDeContarSin(java.util.Collection<Integer> materialesQuitados) {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja()).map(TemaCronograma::getTema)
                .filter(tema -> !tema.getAutoevaluacionesPublicadas().isEmpty())
                .filter(tema -> tema.getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .filter(tema -> tema.getMateriales().stream().noneMatch(m -> !m.getBaja() && !m.getOculto()
                        && !materialesQuitados.contains(m.getIdMaterial())))
                .toList();
    }

    /**
     * Cuenta los temas con material publicado y autoevaluación que quedarían si se quitaran ciertos materiales (por
     * ocultarlos o darlos de baja) y ciertas temas del cronograma.
     *
     * @param materialesQuitados Los identificadores de los materiales que dejarían de estar publicados.
     * @param temasQuitadas   Los identificadores de los temas que dejarían de formar parte del cronograma.
     * @return La cantidad de temas que conservarían material publicado y autoevaluación.
     */
    public int cantidadTemasConMaterialPublicadoSin(java.util.Collection<Integer> materialesQuitados,
            java.util.Collection<Integer> temasQuitadas) {
        return (int) temasCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .map(TemaCronograma::getTema).filter(tema -> !temasQuitadas.contains(tema.getIdTema()))
                .filter(tema -> !tema.getAutoevaluacionesPublicadas().isEmpty())
                .filter(tema -> tema.getMateriales().stream().anyMatch(material -> !material.getBaja()
                        && !material.getOculto() && !materialesQuitados.contains(material.getIdMaterial())))
                .count();
    }

    /**
     * Cuenta los temas vigentes del cronograma.
     *
     * @return La cantidad de temas del cronograma.
     */
    public int getCantidadTemas() {
        return (int) temasCronograma.stream().filter(cronograma -> !cronograma.getBaja()).count();
    }

    /**
     * Obtiene los temas vigentes del cronograma, en orden de dictado.
     *
     * @return Los temas del cronograma ordenadas por su número de orden.
     */
    public List<TemaCronograma> getCronogramaOrdenado() {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja() && !cronograma.getTema().getBaja())
                .sorted(Comparator.comparingInt(TemaCronograma::getNumeroOrden)).toList();
    }

    /**
     * Calcula el último día del programa en el que se dicta un tema (suma de los días de los temas
     * anteriores y la propia).
     *
     * @param temaCronograma El tema del cronograma.
     * @return El número de día en que termina el tema.
     */
    public int getDiaHasta(TemaCronograma temaCronograma) {
        return getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getNumeroOrden() <= temaCronograma.getNumeroOrden())
                .mapToInt(TemaCronograma::getDuracionDias).sum();
    }

    /**
     * Calcula el primer día del programa en el que se dicta un tema.
     *
     * @param temaCronograma El tema del cronograma.
     * @return El número de día en que empieza el tema.
     */
    public int getDiaDesde(TemaCronograma temaCronograma) {
        return getDiaHasta(temaCronograma) - temaCronograma.getDuracionDias() + 1;
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
