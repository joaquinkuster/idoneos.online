package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Docente: Perfil de docente vinculado a un Usuario.
 * Posee su propia PK (id_docente) y una FK a Usuario (id_usuario).
 * Mapea directamente a la tabla "Docente" en base_datos.sql.
 */
@Entity
@Table(name = "Docente")
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente")
    private int idDocente;

    /** Años de experiencia profesional declarados. */
    @Column(name = "anios_experiencia", nullable = false)
    private int aniosExperiencia;

    /** Número de matrícula en la CNV (Comisión Nacional de Valores). */
    @Column(name = "matricula_cnv", length = 50)
    private String matriculaCnv;

    /** Descripción biográfica profesional del docente. */
    @Column(name = "biografia", columnDefinition = "text")
    private String biografia;

    /** Indica si el docente está habilitado para dictar clases. */
    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = false;

    /** Fecha en la que el docente aceptó los términos de uso del Clon IA. */
    @Column(name = "fecha_aceptacion_tyc_clon")
    private LocalDateTime fechaAceptacionTycClon;

    /** ID del avatar de Clon IA asignado al docente. */
    @Column(name = "avatar_id", length = 100)
    private String avatarId;

    /** ID de la voz sintetizada de Clon IA asignada al docente. */
    @Column(name = "voice_id", length = 100)
    private String voiceId;

    /** Usuario base al que pertenece este perfil de docente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Títulos académicos declarados por el docente. */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<TituloDocente> titulos = new ArrayList<>();

    /** Cursos dictados por este docente. */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<Curso> cursos = new ArrayList<>();

    public Docente() {
    }

    public Docente(Usuario usuario) {
        this.usuario = usuario;
        this.aniosExperiencia = 0;
        this.habilitado = true;
    }

    public Docente(Usuario usuario, int aniosExperiencia) {
        this.usuario = usuario;
        this.aniosExperiencia = aniosExperiencia;
    }

    public int getId() {
        return idDocente;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setId(int id) {
        this.idDocente = id;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public int getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(int aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public String getMatriculaCnv() {
        return matriculaCnv;
    }

    public void setMatriculaCnv(String matriculaCnv) {
        this.matriculaCnv = matriculaCnv;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public LocalDateTime getFechaAceptacionTycClon() {
        return fechaAceptacionTycClon;
    }

    /** Alias de compatibilidad para servicios existentes. */
    public LocalDateTime getFechaConsentimientoClon() {
        return fechaAceptacionTycClon;
    }

    public void setFechaAceptacionTycClon(LocalDateTime fechaAceptacionTycClon) {
        this.fechaAceptacionTycClon = fechaAceptacionTycClon;
    }

    public void setFechaConsentimientoClon(LocalDateTime fecha) {
        this.fechaAceptacionTycClon = fecha;
    }

    /**
     * Verifica si el docente puede utilizar Clon IA (tiene fecha de aceptación de TyC registrada).
     */
    public boolean puedeUsarClonIA() {
        return this.fechaAceptacionTycClon != null;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<TituloDocente> getTitulos() {
        return titulos;
    }

    public void setTitulos(List<TituloDocente> titulos) {
        this.titulos = titulos;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }
}

