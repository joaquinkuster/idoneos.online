package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Título universitario o de posgrado de un docente.
 * Un docente puede tener más de uno (ej. Contador y Lic. en Administración).
 */
@Entity
@Table(name = "titulos_docente")
@Getter @Setter
@NoArgsConstructor
public class TituloDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    /**
     * Matrícula del colegio/consejo profesional. Nullable: no todas las profesiones exigen matriculación.
     */
    @Column(name = "matricula_colegio", nullable = true, length = 50)
    private String matriculaColegio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    public TituloDocente(String titulo, String matriculaColegio, Docente docente) {
        this.titulo = titulo;
        this.matriculaColegio = matriculaColegio;
        this.docente = docente;
    }
}
