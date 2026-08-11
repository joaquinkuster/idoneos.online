package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "docente_curso")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente")
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso")
    private Curso curso;

    @Column(name = "es_supervisor")
    private boolean esSupervisor = false;

    public DocenteCurso(Docente docente, Curso curso, boolean esSupervisor) {
        this.docente = docente;
        this.curso = curso;
        this.esSupervisor = esSupervisor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public boolean isEsSupervisor() { return esSupervisor; }
    public boolean getEsSupervisor() { return esSupervisor; }
    public void setEsSupervisor(boolean esSupervisor) { this.esSupervisor = esSupervisor; }
}
