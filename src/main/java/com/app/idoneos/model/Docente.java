package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "docente")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docente {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "anios_experiencia", nullable = false)
    private int aniosExperiencia;

    @Column(name = "matricula_cnv", length = 50)
    private String matriculaCnv;

    @Column(name = "biografia", columnDefinition = "text")
    private String biografia;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<TituloDocente> titulos = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<DictadoDocente> dictadosDocentes = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<Material> materiales = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private List<RespuestaForo> respuestasForo = new ArrayList<>();

    public Docente(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAniosExperiencia() { return aniosExperiencia; }
    public void setAniosExperiencia(int aniosExperiencia) { this.aniosExperiencia = aniosExperiencia; }

    public String getMatriculaCnv() { return matriculaCnv; }
    public void setMatriculaCnv(String matriculaCnv) { this.matriculaCnv = matriculaCnv; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public boolean isHabilitado() { return habilitado; }
    public boolean getHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    @Column(name = "fecha_consentimiento_clon")
    private LocalDateTime fechaConsentimientoClon;

    public LocalDateTime getFechaConsentimientoClon() { return fechaConsentimientoClon; }
    public void setFechaConsentimientoClon(LocalDateTime fechaConsentimientoClon) { this.fechaConsentimientoClon = fechaConsentimientoClon; }

    public List<TituloDocente> getTitulos() { return titulos; }
    public void setTitulos(List<TituloDocente> titulos) { this.titulos = titulos; }

    public List<DictadoDocente> getDictadosDocentes() { return dictadosDocentes; }
    public void setDictadosDocentes(List<DictadoDocente> dictadosDocentes) { this.dictadosDocentes = dictadosDocentes; }

    public List<Material> getMateriales() { return materiales; }
    public void setMateriales(List<Material> materiales) { this.materiales = materiales; }

    public List<ClaseClonIA> getClasesClonIA() { return clasesClonIA; }
    public void setClasesClonIA(List<ClaseClonIA> clasesClonIA) { this.clasesClonIA = clasesClonIA; }

    public List<ClaseEnVivo> getClasesEnVivo() { return clasesEnVivo; }
    public void setClasesEnVivo(List<ClaseEnVivo> clasesEnVivo) { this.clasesEnVivo = clasesEnVivo; }

    public List<RespuestaForo> getRespuestasForo() { return respuestasForo; }
    public void setRespuestasForo(List<RespuestaForo> respuestasForo) { this.respuestasForo = respuestasForo; }

    public boolean puedeUsarClonIA() { return habilitado && fechaConsentimientoClon != null; }

}
