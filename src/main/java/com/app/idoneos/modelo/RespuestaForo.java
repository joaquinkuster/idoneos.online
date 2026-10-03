package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de un docente a una consulta del foro.
 */
@Entity
@Table(name = "RespuestaForo")
@Getter
@Setter
@NoArgsConstructor
public class RespuestaForo {

    /**
     * Identificador único de la respuesta del foro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRespuesta")
    private int idRespuesta;

    /**
     * Relación con ParticipacionDocente.
     */
    @ManyToOne
    @JoinColumn(name = "idParticipacionDocente", nullable = false)
    private ParticipacionDocente participacionDocente;

    /**
     * Relación con ConsultaForo.
     */
    @ManyToOne
    @JoinColumn(name = "idConsulta", nullable = false)
    private ConsultaForo consulta;

    /**
     * Texto de la respuesta.
     */
    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Constructor para crear la respuesta del foro con los datos básicos.
     *
     * @param participacionDocente relación con ParticipacionDocente.
     * @param consulta relación con ConsultaForo.
     * @param texto texto de la respuesta.
     */
    public RespuestaForo(ParticipacionDocente participacionDocente, ConsultaForo consulta, String texto) {
        this.participacionDocente = participacionDocente;
        this.consulta = consulta;
        this.texto = texto;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena de la respuesta del foro.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "RespuestaForo #" + idRespuesta;
    }
}
