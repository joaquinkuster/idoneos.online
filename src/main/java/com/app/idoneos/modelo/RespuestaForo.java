package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de un docente a una consulta del foro.
 */
@Entity
@Table(name = "RespuestaForo")
@Check(name = "ck_respuesta_foro_texto", constraints = "trim(texto) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class RespuestaForo {

    /** Cantidad máxima de caracteres del texto de la respuesta. */
    public static final int LONGITUD_TEXTO = 500;

    /**
     * Identificador único de la respuesta del foro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRespuesta")
    private int idRespuesta;

    /**
     * Relación con DocenteCurso.
     */
    @ManyToOne
    @JoinColumn(name = "idDocenteCurso", nullable = false)
    private DocenteCurso docenteCurso;

    /**
     * Relación con ConsultaForo.
     */
    @ManyToOne
    @JoinColumn(name = "idConsulta", nullable = false)
    private ConsultaForo consulta;

    /**
     * Texto de la respuesta.
     */
    @Column(name = "texto", nullable = false, length = LONGITUD_TEXTO)
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
     * @param docenteCurso relación con DocenteCurso.
     * @param consulta relación con ConsultaForo.
     * @param texto texto de la respuesta.
     */
    public RespuestaForo(DocenteCurso docenteCurso, ConsultaForo consulta, String texto) {
        this.docenteCurso = docenteCurso;
        this.consulta = consulta;
        this.texto = texto;
    }

    /**
     * Obtiene el nombre del docente que respondió.
     *
     * @return El nombre completo del docente.
     */
    public String getAutor() {
        return docenteCurso.getDocente().getUsuario().getNombreCompleto();
    }

    /**
     * Obtiene el usuario del docente que respondió.
     *
     * @return El usuario autor.
     */
    public Usuario getUsuario() {
        return docenteCurso.getDocente().getUsuario();
    }

    /**
     * Indica si un docente puede modificar la respuesta: es el autor, está vigente y está dentro del plazo de edición.
     *
     * @param docente El docente (puede ser {@code null}).
     * @param minutos El plazo de edición, en minutos.
     * @return {@code true} si puede modificarla.
     */
    public boolean puedeModificar(Docente docente, int minutos) {
        return docente != null && !baja && docenteCurso.getDocente().getIdDocente() == docente.getIdDocente()
                && LocalDateTime.now().isBefore(fechaCreacion.plusMinutes(minutos));
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
