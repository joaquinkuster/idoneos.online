package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad ConsultaForo: Pregunta o consulta publicada por un Alumno en el foro de una Unidad.
 * Mapea directamente a la tabla "ConsultaForo" en base_datos.sql.
 */
@Entity
@Table(name = "ConsultaForo")
public class ConsultaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta_foro")
    private int idConsultaForo;

    /** Contenido textual de la consulta. */
    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    /** Fecha y hora de publicación de la consulta. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** Estado de baja lógica de la consulta. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Alumno autor de la consulta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    /** Unidad temática a la que pertenece el foro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public ConsultaForo() {
    }

    public ConsultaForo(String texto, Alumno alumno, Unidad unidad) {
        this.texto = texto;
        this.alumno = alumno;
        this.unidad = unidad;
        this.fecha = LocalDateTime.now();
    }

    public int getId() {
        return idConsultaForo;
    }

    public int getIdConsultaForo() {
        return idConsultaForo;
    }

    public void setId(int id) {
        this.idConsultaForo = id;
    }

    public void setIdConsultaForo(int idConsultaForo) {
        this.idConsultaForo = idConsultaForo;
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

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }
}

