package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad ConsultaForo: Pregunta realizada por un alumno dentro del foro
 * temático de una Unidad.
 * Mapea directamente a la tabla "ConsultaForo" en base_datos.sql.
 */
@Entity
@Table(name = "ConsultaForo")
public class ConsultaForo {

    /** Identificador único de la consulta en el foro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Contenido textual de la pregunta o duda expuesta. */
    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    /** Fecha y hora en la que se publicó la consulta. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** Marca de baja lógica. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática a la que pertenece la duda. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    /** Alumno (Usuario) autor de la consulta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Usuario usuario;

    /** Respuestas docentes asociadas a esta consulta. */
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL)
    private List<RespuestaForo> respuestas = new ArrayList<>();

    public ConsultaForo() {
    }

    public ConsultaForo(String texto, Unidad unidad, Usuario usuario) {
        this.texto = texto;
        this.unidad = unidad;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<RespuestaForo> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<RespuestaForo> respuestas) {
        this.respuestas = respuestas;
    }

    public String getConsulta() {
        return texto;
    }
}
