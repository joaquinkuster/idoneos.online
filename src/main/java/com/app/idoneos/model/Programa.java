package com.app.idoneos.model;

import com.app.idoneos.util.DuracionUtil;
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

/** Entidad que representa un programa de un curso, que es una versión de su diseño. */
@Entity
@Table(name = "Programa")
@Check(name = "ck_programa_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_programa_descripcion", constraints = "descripcion is null or trim(descripcion) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Programa {

    /** Cantidad máxima de caracteres del nombre. */
    public static final int LONGITUD_NOMBRE = 50;

    /** Cantidad máxima de caracteres de la descripción. */
    public static final int LONGITUD_DESCRIPCION = 150;

    /** Identificador único del programa. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPrograma")
    private int idPrograma;

    /** Curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Nombre del programa. */
    @Column(name = "nombre", nullable = false, length = LONGITUD_NOMBRE)
    private String nombre;

    /** Qué cambia esta versión respecto de la anterior. */
    @Column(name = "descripcion", nullable = true, length = LONGITUD_DESCRIPCION)
    private String descripcion;

    /** Programa del que se derivó esta versión ("Versión 3, a partir de Versión 2"). */
    @ManyToOne
    @JoinColumn(name = "idProgramaAnterior", nullable = true)
    private Programa programaAnterior;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Cohortes que referencian a este registro. */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<Cohorte> cohortes = new HashSet<>();

    /** Temas cronograma que referencian a este registro. */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<TemaCronograma> temasCronograma = new HashSet<>();

    /** Crea el programa con los datos básicos. */
    public Programa(Curso curso, String nombre) {
        this.curso = curso;
        this.nombre = nombre;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del programa. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Indica si el programa es el vigente de su curso: el activo de fecha de creación más reciente. */
    public boolean esVigente() {
        Programa vigente = curso.getProgramaVigente();
        return !baja && vigente != null && vigente.getIdPrograma() == idPrograma;
    }

    /** Obtiene el estado del programa dentro de su curso. */
    public String getEstado() {
        if (baja) {
            return "Dado de baja";
        }
        return esVigente() ? "Vigente" : "Anterior";
    }

    /** Cuenta las cohortes activas del programa. */
    public int getCantidadCohortesActivas() {
        return (int) cohortes.stream().filter(cohorte -> !cohorte.getBaja()).count();
    }

    /** Cuenta las inscripciones activas de todas las cohortes del programa. */
    public int getCantidadInscripcionesActivas() {
        return cohortes.stream().mapToInt(Cohorte::getCantidadInscriptos).sum();
    }

    /** Obtiene las cohortes activas del programa, de la más reciente a la más antigua. */
    public List<Cohorte> getCohortesActivas() {
        return cohortes.stream().filter(cohorte -> !cohorte.getBaja())
                .sorted(Comparator.comparingInt(Cohorte::getIdCohorte).reversed()).toList();
    }

    /** Indica si el programa ya no se puede modificar: alguna de sus cohortes está publicada en el catálogo. */
    public boolean estaCongelado() {
        return cohortes.stream().anyMatch(Cohorte::estaPublicada);
    }

    /** Obtiene el motivo por el que el programa no se puede modificar, sin punto final. */
    public String getMotivoDeCongelado() {
        return com.app.idoneos.util.MensajeErrorUtil.MOTIVO_PUBLICADO;
    }

    /** Indica si el programa tiene cohortes que todavía no terminaron (próximas, abiertas o en dictado). */
    public boolean tieneCohortesActivas() {
        return cohortes.stream().anyMatch(Cohorte::estaActiva);
    }

    /** Obtiene los temas del cronograma que no tienen ninguna autoevaluación visible (vigente y no oculta). */
    public List<Tema> getTemasSinAutoevaluacion() {
        return getCronogramaOrdenado().stream().map(TemaCronograma::getTema)
                .filter(tema -> tema.getAutoevaluacionesPublicadas().isEmpty()).toList();
    }

    /** Indica qué le falta al programa para tener el contenido completo. */
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
            faltantes.add("La duración asignada a los temas (" + DuracionUtil.formatear(getDuracionTotalDias())
                    + ") no coincide con la del curso (" + DuracionUtil.formatear(curso.getDuracionDias()) + ")");
        }
        return faltantes;
    }

    /** Indica si la duración del curso está asignada por completo entre los temas del programa. */
    public boolean estaDuracionAsignadaCompleta() {
        return getCantidadTemas() > 0 && getDuracionTotalDias() == curso.getDuracionDias();
    }

    /** Indica si el programa tiene el contenido completo (ver.getFaltantesDeContenido(int)). */
    public boolean tieneContenidoCompleto(int minimoTemasConMaterial) {
        return getFaltantesDeContenido(minimoTemasConMaterial).isEmpty();
    }

    /** Calcula la duración total del cronograma del programa, sumando los días de cada tema vigente. */
    public int getDuracionTotalDias() {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .mapToInt(TemaCronograma::getDuracionDias).sum();
    }

    /** Cantidad máxima de columnas con las que se dibuja el cronograma en un espacio sin barra de desplazamiento. */
    public static final int COLUMNAS_MAXIMAS_DEL_CRONOGRAMA = 20;

    /** Calcula cuántos días representa cada columna al dibujar el cronograma en un espacio sin desplazamiento. */
    public int getPasoCronograma() {
        int total = curso.getDuracionDias();
        if (total <= COLUMNAS_MAXIMAS_DEL_CRONOGRAMA) {
            return 1;
        }
        return total <= COLUMNAS_MAXIMAS_DEL_CRONOGRAMA * DuracionUtil.DIAS_POR_SEMANA
                ? DuracionUtil.DIAS_POR_SEMANA : DuracionUtil.DIAS_POR_MES;
    }

    /** Obtiene el nombre de la unidad que representa cada columna del cronograma. */
    public String getUnidadCronograma() {
        return switch (getPasoCronograma()) {
            case DuracionUtil.DIAS_POR_MES -> "mes";
            case DuracionUtil.DIAS_POR_SEMANA -> "semana";
            default -> "día";
        };
    }

    /** Calcula cuántas columnas tiene el cronograma al dibujarlo (una por unidad, ver.getPasoCronograma()). */
    public int getColumnasCronograma() {
        int paso = getPasoCronograma();
        return (curso.getDuracionDias() + paso - 1) / paso;
    }

    /** Cuenta los temas del cronograma con contenido publicado. */
    public int getCantidadTemasConMaterialPublicado() {
        return cantidadTemasConMaterialPublicadoSin(java.util.Set.of(), java.util.Set.of());
    }

    /** Obtiene los temas que hoy cuentan (con material publicado y autoevaluación) y dejarían de contar si se quitaran ciertos materiales: las que se quedarían sin ningún material publicado. */
    public java.util.List<Tema> temasQueDejanDeContarSin(java.util.Collection<Integer> materialesQuitados) {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja()).map(TemaCronograma::getTema)
                .filter(tema -> !tema.getAutoevaluacionesPublicadas().isEmpty())
                .filter(tema -> tema.getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .filter(tema -> tema.getMateriales().stream().noneMatch(m -> !m.getBaja() && !m.getOculto()
                        && !materialesQuitados.contains(m.getIdTemaRecurso())))
                .toList();
    }

    /** Cuenta los temas con material publicado y autoevaluación que quedarían si se quitaran ciertos materiales (por ocultarlos o darlos de baja) y ciertas temas del cronograma. */
    public int cantidadTemasConMaterialPublicadoSin(java.util.Collection<Integer> materialesQuitados,
            java.util.Collection<Integer> temasQuitadas) {
        return (int) temasCronograma.stream().filter(cronograma -> !cronograma.getBaja())
                .map(TemaCronograma::getTema).filter(tema -> !temasQuitadas.contains(tema.getIdTema()))
                .filter(tema -> !tema.getAutoevaluacionesPublicadas().isEmpty())
                .filter(tema -> tema.getMateriales().stream().anyMatch(material -> !material.getBaja()
                        && !material.getOculto() && !materialesQuitados.contains(material.getIdTemaRecurso())))
                .count();
    }

    /** Cuenta los temas vigentes del cronograma. */
    public int getCantidadTemas() {
        return (int) temasCronograma.stream().filter(cronograma -> !cronograma.getBaja()).count();
    }

    /** Obtiene los temas vigentes del cronograma, en orden de dictado. */
    public List<TemaCronograma> getCronogramaOrdenado() {
        return temasCronograma.stream().filter(cronograma -> !cronograma.getBaja() && !cronograma.getTema().getBaja())
                .sorted(Comparator.comparingInt(TemaCronograma::getNumeroOrden)).toList();
    }

    /** Calcula el último día del programa en el que se dicta un tema (suma de los días de los temas anteriores y la propia). */
    public int getDiaHasta(TemaCronograma temaCronograma) {
        return getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getNumeroOrden() <= temaCronograma.getNumeroOrden())
                .mapToInt(TemaCronograma::getDuracionDias).sum();
    }

    /** Calcula el primer día del programa en el que se dicta un tema. */
    public int getDiaDesde(TemaCronograma temaCronograma) {
        return getDiaHasta(temaCronograma) - temaCronograma.getDuracionDias() + 1;
    }

    /** Elige la cohorte más relevante del programa: la que está en dictado. */
    public Cohorte getCohorteDestacada() {
        // Las cohortes pendientes o canceladas no se muestran: todavía no tienen contenido completo o ya no lo tendrán
        List<Cohorte> vigentes = cohortes.stream().filter(cohorte -> !cohorte.getBaja() && cohorte.esActiva()).toList();
        for (String estado : List.of(EstadoCohorte.EN_DICTADO, EstadoCohorte.ABIERTA)) {
            Cohorte elegida = vigentes.stream().filter(cohorte -> estado.equals(cohorte.getEstado()))
                    .max(Comparator.comparingInt(Cohorte::getIdCohorte)).orElse(null);
            if (elegida != null) {
                return elegida;
            }
        }
        return vigentes.stream().max(Comparator.comparingInt(Cohorte::getIdCohorte)).orElse(null);
    }
}
