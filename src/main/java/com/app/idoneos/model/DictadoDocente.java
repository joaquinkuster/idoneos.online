package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dictado_docente")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictadoDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictado_id", nullable = false)
    private Dictado dictado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    public DictadoDocente(Dictado dictado, Docente docente) {
        this.dictado = dictado;
        this.docente = docente;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Dictado getDictado() { return dictado; }
    public void setDictado(Dictado dictado) { this.dictado = dictado; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
}
