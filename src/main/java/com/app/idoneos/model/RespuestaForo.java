package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respuesta_foro")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaForo {

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
    @JoinColumn(name = "consulta_foro_id", nullable = false)
    private ConsultaForo consultaForo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public ConsultaForo getConsultaForo() { return consultaForo; }
    public void setConsultaForo(ConsultaForo consultaForo) { this.consultaForo = consultaForo; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }


    public RespuestaForo(String texto, ConsultaForo consultaForo, Docente docente) {
        this.texto = texto;
        this.consultaForo = consultaForo;
        this.docente = docente;
    }
    public ConsultaForo getConsulta() { return consultaForo; }

}
