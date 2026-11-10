package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la respuesta de un docente a una consulta del foro. */
@Entity
@Table(name = "RespuestaForo")
@Check(name = "ck_respuesta_foro_texto", constraints = "trim(texto) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class RespuestaForo {

    /** Cantidad máxima de caracteres del texto de la respuesta. */
    public static final int LONGITUD_TEXTO = 500;

    /** Identificador único de la respuesta del foro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRespuesta")
    private int idRespuesta;

    /** Docente curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idDocenteCurso", nullable = false)
    private DocenteCurso docenteCurso;

    /** Consulta de este registro. */
    @ManyToOne
    @JoinColumn(name = "idConsulta", nullable = false)
    private ConsultaForo consulta;

    /** Texto de la respuesta. */
    @Column(name = "texto", nullable = false, length = LONGITUD_TEXTO)
    private String texto;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea la respuesta del foro con los datos básicos. */
    public RespuestaForo(DocenteCurso docenteCurso, ConsultaForo consulta, String texto) {
        this.docenteCurso = docenteCurso;
        this.consulta = consulta;
        this.texto = texto;
    }

    /** Obtiene el nombre del docente que respondió. */
    public String getAutor() {
        return docenteCurso.getDocente().getUsuario().getNombreCompleto();
    }

    /** Obtiene el usuario del docente que respondió. */
    public Usuario getUsuario() {
        return docenteCurso.getDocente().getUsuario();
    }

    /** Indica si un docente puede modificar la respuesta: es el autor. */
    public boolean puedeModificar(Docente docente, int minutos) {
        return docente != null && !baja && docenteCurso.getDocente().getIdDocente() == docente.getIdDocente()
                && LocalDateTime.now().isBefore(fechaCreacion.plusMinutes(minutos));
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la respuesta del foro. */
    @Override
    public String toString() {
        return "RespuestaForo #" + idRespuesta;
    }
}
