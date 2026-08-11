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

    public LocalDateTime getFechaConsentimientoClon() {
        return null;
    }
}
