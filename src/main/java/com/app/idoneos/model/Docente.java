package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Docente: Subtipo de Usuario con rol docente (relación 1 a 0..1
 * mediante clave compartida).
 * Mapea directamente a la tabla "Docente" en base_datos.sql.
 */
@Entity
@Table(name = "Docente")
public class Docente {

    /**
     * Identificador del docente, coincidente con el id de Usuario (clave primaria
     * compartida).
     */
    @Id
    @Column(name = "id")
    private int id;

    /** Años de experiencia profesional declarados. */
    @Column(name = "anios_experiencia", nullable = false)
    private int aniosExperiencia = 0;

    /**
     * Matrícula profesional del Registro de Idóneos de la Comisión Nacional de
     * Valores (opcional).
     */
    @Column(name = "matricula_cnv", length = 50)
    private String matriculaCnv;

    /** Biografía y trayectoria del docente. */
    @Column(name = "biografia", columnDefinition = "text")
    private String biografia;

    /** Indicador de si el docente se encuentra habilitado para dictar clases. */
    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    /**
     * Relación 1 a 1 con la entidad base Usuario (@MapsId vincula la PK con
     * Usuario).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    /** Títulos universitarios o profesionales registrados del docente. */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<TituloDocente> titulos = new ArrayList<>();

    /** Asignaciones a dictados de cursos (como titular o supervisor). */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<DictadoDocente> dictadosDocentes = new ArrayList<>();

    @Column(name = "fecha_consentimiento_clon")
    private LocalDateTime fechaConsentimientoClon;

    public Docente() {
    }

    public Docente(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean puedeUsarClonIA() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
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

    public List<DictadoDocente> getDictadosDocentes() {
        return dictadosDocentes;
    }

    public void setDictadosDocentes(List<DictadoDocente> dictadosDocentes) {
        this.dictadosDocentes = dictadosDocentes;
    }

    public LocalDateTime getFechaConsentimientoClon() {
        return fechaConsentimientoClon;
    }

    public void setFechaConsentimientoClon(LocalDateTime fechaConsentimientoClon) {
        this.fechaConsentimientoClon = fechaConsentimientoClon;
    }
}
