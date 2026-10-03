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
@Table(name = "ParticipacionDocente")
@Getter
@Setter
@NoArgsConstructor
public class ParticipacionDocente {

    /**
     * Identificador único de la participación docente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idParticipacionDocente")
    private int idParticipacionDocente;

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
     * Relación con Programa. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idProgramaPorDefecto", nullable = true)
    private Programa programaPorDefecto;

    /**
     * Relación con Cohorte. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idCohortePorDefecto", nullable = true)
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
     * Conjunto de materiales asociados.
     */
    @OneToMany(mappedBy = "participacionDocente", cascade = CascadeType.ALL)
    private Set<Material> materiales = new HashSet<>();

    /**
     * Conjunto de clasesEnVivo asociados.
     */
    @OneToMany(mappedBy = "participacionDocente", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /**
     * Conjunto de clasesClon asociados.
     */
    @OneToMany(mappedBy = "participacionDocente", cascade = CascadeType.ALL)
    private Set<ClaseClon> clasesClon = new HashSet<>();

    /**
     * Conjunto de respuestasForo asociados.
     */
    @OneToMany(mappedBy = "participacionDocente", cascade = CascadeType.ALL)
    private Set<RespuestaForo> respuestasForo = new HashSet<>();

    /**
     * Constructor para crear la participación docente con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param docente relación con Docente.
     * @param esTitular indica si el docente participa como titular. Si es "false", participa como ayudante.
     */
    public ParticipacionDocente(Curso curso, Docente docente, Boolean esTitular) {
        this.curso = curso;
        this.docente = docente;
        this.esTitular = esTitular;
    }

    /**
     * Devuelve una representación en forma de cadena de la participación docente.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "ParticipacionDocente #" + idParticipacionDocente;
    }
}
