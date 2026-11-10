package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la participación de un docente en un curso y, opcionalmente, en un programa o cohorte. */
@Entity
@Table(name = "DocenteCurso")
@Getter
@Setter
@NoArgsConstructor
public class DocenteCurso {

    /** Identificador único de la participación docente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocenteCurso")
    private int idDocenteCurso;

    /** Curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Docente de este registro. */
    @ManyToOne
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    /** Cohorte por defecto de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = true)
    private Cohorte cohortePorDefecto;

    /** Indica si el docente participa como titular. */
    @Column(name = "esTitular", nullable = false)
    private Boolean esTitular = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Conjunto de recursos que el docente incorporó a los temas del curso. */
    @OneToMany(mappedBy = "docenteCurso", cascade = CascadeType.ALL)
    private Set<TemaRecurso> temasRecurso = new HashSet<>();

    /** Clases en vivo que referencian a este registro. */
    @OneToMany(mappedBy = "docenteCurso", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /** Respuestas foro que referencian a este registro. */
    @OneToMany(mappedBy = "docenteCurso", cascade = CascadeType.ALL)
    private Set<RespuestaForo> respuestasForo = new HashSet<>();

    /** Crea la participación docente con los datos básicos. */
    public DocenteCurso(Curso curso, Docente docente, Boolean esTitular) {
        this.curso = curso;
        this.docente = docente;
        this.esTitular = esTitular;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la participación docente. */
    @Override
    public String toString() {
        return "DocenteCurso #" + idDocenteCurso;
    }

    /** Indica si la cohorte de su contexto de trabajo sigue sirviendo. */
    private boolean tieneCohorteDeTrabajoVigente() {
        return cohortePorDefecto != null && !cohortePorDefecto.getBaja() && !cohortePorDefecto.esCancelada()
                && cohortePorDefecto.getCurso().getIdCurso() == curso.getIdCurso();
    }

    /** Obtiene el programa sobre el que trabaja el docente. */
    public Programa getProgramaDeTrabajo() {
        return tieneCohorteDeTrabajoVigente() ? cohortePorDefecto.getPrograma() : curso.getProgramaVigente();
    }

    /** Obtiene la cohorte sobre la que trabaja el docente: la de su contexto de trabajo, si sigue vigente. */
    public Cohorte getCohorteDeTrabajo() {
        if (tieneCohorteDeTrabajoVigente()) {
            return cohortePorDefecto;
        }
        Programa programa = curso.getProgramaVigente();
        Cohorte destacada = programa == null ? null : programa.getCohorteDestacada();
        if (destacada != null) {
            return destacada;
        }
        // Sin una cohorte vigente del programa, la más reciente del curso (por ejemplo una pendiente sin programa)
        return curso.getCohortesDeTrabajo().stream().findFirst().orElse(null);
    }
}
