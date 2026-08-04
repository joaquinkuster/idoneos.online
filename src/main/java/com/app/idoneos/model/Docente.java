package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Subtipo de Usuario para docentes.
 * Contiene los atributos propios del perfil profesional del docente.
 */
@Entity
@Table(name = "docente")
@Getter @Setter
@NoArgsConstructor
public class Docente {

    @Id
    @Column(name = "id_usuario")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    /**
     * Fecha en que el docente autorizó el uso de su imagen/voz para Clon IA.
     * Null indica que no dio consentimiento todavía.
     * DDL: timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_consentimiento_clon", nullable = true)
    private LocalDateTime fechaConsentimientoClon;

    @Column(name = "anios_experiencia", nullable = true)
    private Integer aniosExperiencia;

    /**
     * Matrícula del Registro de Idóneos de la CNV. Opcional.
     */
    @Column(name = "matricula_cnv", nullable = true, length = 50)
    private String matriculaCnv;

    @Column(name = "biografia", nullable = true, columnDefinition = "TEXT")
    private String biografia;

    /**
     * false = deshabilitado temporalmente (ej. suspensión). true = puede dictar.
     */
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TituloDocente> titulos = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<DocenteCurso> cursos = new ArrayList<>();

    public Docente(Usuario usuario) {
        this.usuario = usuario;
        this.id = usuario.getId();
    }

    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }

    public boolean puedeUsarClonIA() {
        return habilitado && fechaConsentimientoClon != null;
    }
}
