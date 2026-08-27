package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad RespuestaForo: Respuesta brindada por un Docente a una ConsultaForo.
 * Mapea directamente a la tabla "RespuestaForo" en base_datos.sql.
 */
@Entity
@Table(name = "RespuestaForo")
public class RespuestaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta_foro")
    private int idRespuestaForo;

    /** Contenido textual de la respuesta. */
    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    /** Fecha y hora en la que se respondió la consulta. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** Estado de baja lógica de la respuesta. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Docente autor de la respuesta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    /** Consulta original a la que responde. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta_foro", nullable = false)
    private ConsultaForo consulta;

    public RespuestaForo() {
    }

    public RespuestaForo(String texto, ConsultaForo consulta, Docente docente) {
        this.texto = texto;
        this.consulta = consulta;
        this.docente = docente;
        this.fecha = LocalDateTime.now();
    }

    public int getId() {
        return idRespuestaForo;
    }

    public int getIdRespuestaForo() {
        return idRespuestaForo;
    }

    public void setId(int id) {
        this.idRespuestaForo = id;
    }

    public void setIdRespuestaForo(int idRespuestaForo) {
        this.idRespuestaForo = idRespuestaForo;
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

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public ConsultaForo getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaForo consulta) {
        this.consulta = consulta;
    }
}

