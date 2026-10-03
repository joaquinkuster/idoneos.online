package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Edición de un programa con sus fechas de inscripción y dictado, cupo y duración del acceso.
 */
@Entity
@Table(name = "Cohorte")
@Getter
@Setter
@NoArgsConstructor
public class Cohorte {

    /**
     * Identificador único de la cohorte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCohorte")
    private int idCohorte;

    /**
     * Relación con Programa.
     */
    @ManyToOne
    @JoinColumn(name = "idPrograma", nullable = false)
    private Programa programa;

    /**
     * Fecha y hora de inicio del período de inscripción.
     */
    @Column(name = "fechaInicioInscripcion", nullable = false)
    private LocalDateTime fechaInicioInscripcion;

    /**
     * Fecha y hora de fin del período de inscripción.
     */
    @Column(name = "fechaFinInscripcion", nullable = false)
    private LocalDateTime fechaFinInscripcion;

    /**
     * Fecha y hora de inicio del dictado. Es opcional.
     */
    @Column(name = "fechaInicioDictado", nullable = true)
    private LocalDateTime fechaInicioDictado;

    /**
     * Fecha y hora de fin del dictado. Es opcional.
     */
    @Column(name = "fechaFinDictado", nullable = true)
    private LocalDateTime fechaFinDictado;

    /**
     * Cupo máximo de inscriptos. Es opcional.
     */
    @Column(name = "cupoMaximo", nullable = true)
    private Integer cupoMaximo;

    /**
     * Cantidad de semanas de acceso al contenido luego de inscribirse.
     */
    @Column(name = "semanasAcceso", nullable = false)
    private int semanasAcceso;

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
     * Conjunto de clasesEnVivo asociados.
     */
    @OneToMany(mappedBy = "cohorte", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /**
     * Conjunto de inscripciones asociados.
     */
    @OneToMany(mappedBy = "cohorte", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "cohortePorDefecto", cascade = CascadeType.ALL)
    private Set<ParticipacionDocente> participacionesDocente = new HashSet<>();

    /**
     * Constructor para crear la cohorte con los datos básicos.
     *
     * @param programa relación con Programa.
     * @param fechaInicioInscripcion fecha y hora de inicio del período de inscripción.
     * @param fechaFinInscripcion fecha y hora de fin del período de inscripción.
     * @param semanasAcceso cantidad de semanas de acceso al contenido luego de inscribirse.
     */
    public Cohorte(Programa programa, LocalDateTime fechaInicioInscripcion, LocalDateTime fechaFinInscripcion, int semanasAcceso) {
        this.programa = programa;
        this.fechaInicioInscripcion = fechaInicioInscripcion;
        this.fechaFinInscripcion = fechaFinInscripcion;
        this.semanasAcceso = semanasAcceso;
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
     * Devuelve una representación en forma de cadena de la cohorte.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Cohorte #" + idCohorte;
    }

    /**
     * Calcula el estado de la cohorte según sus fechas: Próxima, Abierta (con inscripción abierta),
     * En dictado o Finalizada.
     *
     * @return El estado de la cohorte.
     */
    public String getEstado() {
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(fechaInicioInscripcion)) {
            return "Próxima";
        }
        if (!ahora.isAfter(fechaFinInscripcion)) {
            return "Abierta";
        }
        LocalDateTime fin = fechaFinDictado != null ? fechaFinDictado : fechaFinInscripcion.plusWeeks(semanasAcceso);
        return ahora.isAfter(fin) ? "Finalizada" : "En dictado";
    }

    /**
     * Verifica si la cohorte tiene la inscripción abierta.
     *
     * @return {@code true} si la inscripción está abierta.
     */
    public boolean estaAbierta() {
        return "Abierta".equals(getEstado());
    }

    /**
     * Cuenta las inscripciones vigentes (no dadas de baja) de la cohorte.
     *
     * @return La cantidad de inscriptos.
     */
    public int getCantidadInscriptos() {
        return (int) inscripciones.stream().filter(inscripcion -> !inscripcion.getBaja()).count();
    }

    /**
     * Calcula el cupo disponible de la cohorte.
     *
     * @return El cupo disponible, o {@code null} si la cohorte no tiene cupo máximo.
     */
    public Integer getCupoDisponible() {
        return cupoMaximo == null ? null : Math.max(0, cupoMaximo - getCantidadInscriptos());
    }
}
