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
 * Programa de un curso: una versión de su diseño, con el cronograma de temas que reparte la duración del curso.
 */
@Entity
@Table(name = "Programa")
@Check(name = "ck_programa_nombre", constraints = "trim(nombre) <> ''")
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
     * Nombre del programa. Por defecto es "Versión N", con N la cantidad de versiones que tuvo el curso.
     */
    @Column(name = "nombre", nullable = false, length = LONGITUD_NOMBRE)
    private String nombre;

    /**
     * Qué cambia esta versión respecto de la anterior.
     */
    @Column(name = "descripcion", nullable = true, length = LONGITUD_DESCRIPCION)
    private String descripcion;

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
     * Constructor para crear el programa con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param nombre nombre del programa.
     */
    public Programa(Curso curso, String nombre) {
        this.curso = curso;
        this.nombre = nombre;
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
     * Indica qué le falta al programa para tener el contenido completo, que es lo que se necesita para que una cohorte
     * esté activa: los datos académicos del curso completos, el mínimo de temas con material y autoevaluación publicados, una
     * autoevaluación visible en cada tema y la duración del curso asignada por completo entre sus temas.
     *
     * @param minimoTemasConMaterial El mínimo de temas con material publicado y autoevaluación que exige el sistema.
     * @return Lo que falta, uno por línea y listo para mostrar; vacío si el contenido está completo.
     */
    public List<String> getFaltantesDeContenido(int minimoTemasConMaterial) {
        List<String> faltantes = new java.util.ArrayList<>();
        List<String> faltantesDelCurso = curso.getFaltantesDeDatosAcademicos();
        if (!faltantesDelCurso.isEmpty()) {
            faltantes.add("Faltan completar los datos del curso: " + String.join(", ", faltantesDelCurso));
        }
        int conMaterial = getCantidadTemasConMaterialPublicado();
        if (conMaterial < minimoTemasConMaterial) {
            faltantes.add("Faltan temas con al menos un material y una autoevaluación publicados: tiene " + conMaterial
                    + " de " + minimoTemasConMaterial + " requeridos");
        }
        List<Tema> sinAutoevaluacion = getTemasSinAutoevaluacion();
        if (!sinAutoevaluacion.isEmpty()) {
            faltantes.add("Faltan autoevaluaciones visibles en: " + String.join(", ",
                    sinAutoevaluacion.stream().map(tema -> "«" + tema.getTitulo() + "»").toList()));
        }
        if (!estaDuracionAsignadaCompleta()) {
            faltantes.add("La duración asignada a los temas (" + DuracionUtilidad.formatear(getDuracionTotalDias())
                    + ") no coincide con la del curso (" + DuracionUtilidad.formatear(curso.getDuracionDias()) + ")");
        }
        return faltantes;
    }

    /**
     * Indica si la duración del curso está asignada por completo entre los temas del programa: la suma de los días de sus
     * temas es igual a la duración del curso.
     *
     * @return {@code true} si el programa cubre toda la duración del curso.
     */
    public boolean estaDuracionAsignadaCompleta() {
        return getCantidadTemas() > 0 && getDuracionTotalDias() == curso.getDuracionDias();
    }

    /**
     * Indica si el programa tiene el contenido completo (ver {@link #getFaltantesDeContenido(int)}).
     *
     * @param minimoTemasConMaterial El mínimo de temas con material publicado y autoevaluación que exige el sistema.
     * @return {@code true} si no le falta nada.
     */
    public boolean tieneContenidoCompleto(int minimoTemasConMaterial) {
        return getFaltantesDeContenido(minimoTemasConMaterial).isEmpty();
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

    /** Cantidad máxima de columnas con las que se dibuja el cronograma en un espacio sin barra de desplazamiento. */
    public static final int COLUMNAS_MAXIMAS_DEL_CRONOGRAMA = 20;

    /**
     * Calcula cuántos días representa cada columna al dibujar el cronograma en un espacio sin desplazamiento: la escala
     * más fina (día, semana o mes) con la que la duración total no pasa de {@value #COLUMNAS_MAXIMAS_DEL_CRONOGRAMA}
     * columnas. Un tema que no dura una cantidad exacta de columnas ocupa la columna parcial completa.
     *
     * @return 1, 7 o 30.
     */
    public int getPasoCronograma() {
        int total = curso.getDuracionDias();
        if (total <= COLUMNAS_MAXIMAS_DEL_CRONOGRAMA) {
            return 1;
        }
        return total <= COLUMNAS_MAXIMAS_DEL_CRONOGRAMA * DuracionUtilidad.DIAS_POR_SEMANA
                ? DuracionUtilidad.DIAS_POR_SEMANA : DuracionUtilidad.DIAS_POR_MES;
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
        return (curso.getDuracionDias() + paso - 1) / paso;
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
                        && !materialesQuitados.contains(m.getIdTemaRecurso())))
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
                        && !material.getOculto() && !materialesQuitados.contains(material.getIdTemaRecurso())))
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
        // Las cohortes pendientes o canceladas no se muestran: todavía no tienen contenido completo o ya no lo tendrán
        List<Cohorte> vigentes = cohortes.stream().filter(cohorte -> !cohorte.getBaja() && cohorte.esActiva()).toList();
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
