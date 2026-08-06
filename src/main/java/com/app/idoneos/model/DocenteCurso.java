package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tabla asociativa M:N entre Docente y Curso.
 * esSupervisor=false → titular del curso; esSupervisor=true → supervisor.
 * La regla "exactamente un titular por curso" se valida en la aplicación, no en BD.
 */
@Entity
@Table(name = "docente_curso",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_docente", "id_curso"}))
@Getter @Setter
@NoArgsConstructor
public class DocenteCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    /**
     * false = titular, true = supervisor.
     */
    @Column(name = "es_supervisor", nullable = false)
    private boolean esSupervisor = false;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public boolean isEsSupervisor() { return esSupervisor; }
    public boolean getEsSupervisor() { return esSupervisor; }
    public void setEsSupervisor(boolean esSupervisor) { this.esSupervisor = esSupervisor; }

    public DocenteCurso(Docente docente, Curso curso, boolean esSupervisor) {
        this.docente = docente;
        this.curso = curso;
        this.esSupervisor = esSupervisor;
    }
}
