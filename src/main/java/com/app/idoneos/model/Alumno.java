package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumno")
@Getter @Setter
public class Alumno {

    @Id
    @Column(name = "id")
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<ConsultaForo> consultasForo = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<IntentoAutoevaluacion> intentosAutoevaluacion = new ArrayList<>();

    /** No-arg constructor required by JPA/Hibernate. */
    public Alumno() {}

    /** Convenience constructor for service code. */
    public Alumno(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Inscripcion> getInscripciones() { return inscripciones; }
    public void setInscripciones(List<Inscripcion> inscripciones) { this.inscripciones = inscripciones; }

    public List<ConsultaForo> getConsultasForo() { return consultasForo; }
    public void setConsultasForo(List<ConsultaForo> consultasForo) { this.consultasForo = consultasForo; }

    public List<IntentoAutoevaluacion> getIntentosAutoevaluacion() { return intentosAutoevaluacion; }
    public void setIntentosAutoevaluacion(List<IntentoAutoevaluacion> intentosAutoevaluacion) { this.intentosAutoevaluacion = intentosAutoevaluacion; }
}
