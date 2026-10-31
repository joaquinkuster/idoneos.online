package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Participación de un docente en un curso, y opcionalmente en un programa o cohorte, como titular o ayudante.
 */
@Entity
@Table(name = "DocenteCurso")
@Getter
@Setter
@NoArgsConstructor
public class DocenteCurso {

    /**
     * Identificador único de la participación docente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocenteCurso")
    private int idDocenteCurso;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Relación con Docente.
     */
    @ManyToOne
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    /**
     * Relación con Cohorte. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = true)
    private Cohorte cohortePorDefecto;

    /**
     * Indica si el docente participa como titular. Si es "false", participa como ayudante.
     */
    @Column(name = "esTitular", nullable = false)
    private Boolean esTitular = false;

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
     * Conjunto de recursos que el docente incorporó a los temas del curso.
     */
    @OneToMany(mappedBy = "docenteCurso", cascade = CascadeType.ALL)
    private Set<TemaRecurso> temasRecurso = new HashSet<>();

    /**
     * Conjunto de clasesEnVivo asociados.
     */
    @OneToMany(mappedBy = "docenteCurso", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /**
     * Conjunto de respuestasForo asociados.
     */
    @OneToMany(mappedBy = "docenteCurso", cascade = CascadeType.ALL)
    private Set<RespuestaForo> respuestasForo = new HashSet<>();

    /**
     * Constructor para crear la participación docente con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param docente relación con Docente.
     * @param esTitular indica si el docente participa como titular. Si es "false", participa como ayudante.
     */
    public DocenteCurso(Curso curso, Docente docente, Boolean esTitular) {
        this.curso = curso;
        this.docente = docente;
        this.esTitular = esTitular;
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
     * Devuelve una representación en forma de cadena de la participación docente.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "DocenteCurso #" + idDocenteCurso;
    }

    /**
     * Indica si la cohorte de su contexto de trabajo sigue sirviendo: no está dada de baja, es del curso y tiene programa.
     */
    private boolean tieneCohorteDeTrabajoVigente() {
        return cohortePorDefecto != null && !cohortePorDefecto.getBaja() && cohortePorDefecto.tienePrograma()
                && cohortePorDefecto.getCurso().getIdCurso() == curso.getIdCurso();
    }

    /**
     * Obtiene el programa sobre el que trabaja el docente: el de su cohorte de trabajo o, si no tiene, el programa
     * vigente del curso.
     *
     * @return El programa de trabajo, o {@code null} si el curso no tiene programas vigentes.
     */
    public Programa getProgramaDeTrabajo() {
        return tieneCohorteDeTrabajoVigente() ? cohortePorDefecto.getPrograma() : curso.getProgramaVigente();
    }

    /**
     * Obtiene la cohorte sobre la que trabaja el docente: la de su contexto de trabajo, si sigue vigente, o la cohorte
     * más relevante del programa vigente del curso.
     *
     * @return La cohorte de trabajo, o {@code null} si el programa no tiene cohortes vigentes.
     */
    public Cohorte getCohorteDeTrabajo() {
        if (tieneCohorteDeTrabajoVigente()) {
            return cohortePorDefecto;
        }
        Programa programa = curso.getProgramaVigente();
        return programa == null ? null : programa.getCohorteDestacada();
    }
}
