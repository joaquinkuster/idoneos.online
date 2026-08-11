package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intento_autoevaluacion")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentoAutoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "nota", nullable = false)
    private double nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoevaluacion_id", nullable = false)
    private Autoevaluacion autoevaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @OneToMany(mappedBy = "intentoAutoevaluacion", cascade = CascadeType.ALL)
    private List<RespuestaIntento> respuestasIntentos = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }

    public Autoevaluacion getAutoevaluacion() { return autoevaluacion; }
    public void setAutoevaluacion(Autoevaluacion autoevaluacion) { this.autoevaluacion = autoevaluacion; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public List<RespuestaIntento> getRespuestasIntentos() { return respuestasIntentos; }
    public void setRespuestasIntentos(List<RespuestaIntento> respuestasIntentos) { this.respuestasIntentos = respuestasIntentos; }


    public IntentoAutoevaluacion(Autoevaluacion autoevaluacion, Usuario usuario) {
        this.autoevaluacion = autoevaluacion;
        // alumno se obtiene desde usuario
        this.alumno = new Alumno(usuario);
    }

}
