package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad TituloDocente: Título universitario o de posgrado declarado por un Docente.
 * Mapea directamente a la tabla "TituloDocente" en base_datos.sql.
 */
@Entity
@Table(name = "TituloDocente")
public class TituloDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_titulo_docente")
    private int idTituloDocente;

    /** Nombre del título universitario o técnico obtenido. */
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    /** Matrícula otorgada por el colegio o consejo profesional (opcional). */
    @Column(name = "matricula_colegio", length = 50)
    private String matriculaColegio;

    /** Docente titular del certificado o título. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    public TituloDocente() {
    }

    public TituloDocente(String titulo, String matriculaColegio, Docente docente) {
        this.titulo = titulo;
        this.matriculaColegio = matriculaColegio;
        this.docente = docente;
    }

    public int getId() {
        return idTituloDocente;
    }

    public int getIdTituloDocente() {
        return idTituloDocente;
    }

    public void setId(int id) {
        this.idTituloDocente = id;
    }

    public void setIdTituloDocente(int idTituloDocente) {
        this.idTituloDocente = idTituloDocente;
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

