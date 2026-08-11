package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad TítuloDocente: Título universitario o de posgrado declarado por un
 * Docente (relación 1 a N).
 * Mapea directamente a la tabla "TituloDocente" en base_datos.sql.
 */
@Entity
@Table(name = "TituloDocente")
public class TituloDocente {

    /** Identificador único del título académico. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * Nombre del título universitario o técnico obtenido (ej. "Contador Público").
     */
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    /** Matrícula otorgada por el colegio o consejo profesional (opcional). */
    @Column(name = "matricula_colegio", length = 50)
    private String matriculaColegio;

    /** Docente titular del certificado o título. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    public TituloDocente() {
    }

    public TituloDocente(String titulo, String matriculaColegio, Docente docente) {
        this.titulo = titulo;
        this.matriculaColegio = matriculaColegio;
        this.docente = docente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMatriculaColegio() {
        return matriculaColegio;
    }

    public void setMatriculaColegio(String matriculaColegio) {
        this.matriculaColegio = matriculaColegio;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }
}
