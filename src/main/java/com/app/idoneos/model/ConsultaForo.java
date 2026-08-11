package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consulta_foro")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @OneToMany(mappedBy = "consultaForo", cascade = CascadeType.ALL)
    private List<RespuestaForo> respuestasForo = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public List<RespuestaForo> getRespuestasForo() { return respuestasForo; }
    public void setRespuestasForo(List<RespuestaForo> respuestasForo) { this.respuestasForo = respuestasForo; }

    public Usuario getUsuario() { return alumno != null ? alumno.getUsuario() : null; }
    public void setUsuario(Usuario u) { if (alumno == null) alumno = new Alumno(u); else alumno.setUsuario(u); }
    public ConsultaForo getConsulta() { return this; }



    public ConsultaForo(String texto, Unidad unidad, Usuario usuario) {
        this.texto = texto;
        this.unidad = unidad;
        this.alumno = new Alumno(usuario);
    }

}
