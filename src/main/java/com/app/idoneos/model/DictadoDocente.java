package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Representa la tabla asociativa entre Dictado y Docente ("Dictado Docente").
 * Permite asignar un equipo docente a un dictado puntual de un programa.
 */
@Entity
@Table(name = "\"Dictado Docente\"")
public class DictadoDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * Dictado al que pertenece el docente asignado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictado_id")
    private Dictado dictado;

    /**
     * Docente asignado al dictado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    /**
     * Distingue si el docente participa como supervisor (false) o titular (true) del dictado.
     */
    @Column(name = "es_supervisor")
    private boolean esSupervisor = false;

    public DictadoDocente() {}

    public DictadoDocente(Dictado dictado, Docente docente, boolean esSupervisor) {
        this.dictado = dictado;
        this.docente = docente;
        this.esSupervisor = esSupervisor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Dictado getDictado() { return dictado; }
    public void setDictado(Dictado dictado) { this.dictado = dictado; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public boolean isEsSupervisor() { return esSupervisor; }
    public boolean getEsSupervisor() { return esSupervisor; }
    public void setEsSupervisor(boolean esSupervisor) { this.esSupervisor = esSupervisor; }
}
