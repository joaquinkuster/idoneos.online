package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una clase dictada en vivo para una cohorte, con transmisión por RTMP. */
@Entity
@Table(name = "ClaseEnVivo")
@Check(name = "ck_clase_en_vivo_titulo", constraints = "trim(titulo) <> ''")
@Check(name = "ck_clase_en_vivo_duracion", constraints = "duracion_minutos > 0")
@Getter
@Setter
@NoArgsConstructor
public class ClaseEnVivo {

    /** Identificador único de la clase en vivo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClaseEnVivo")
    private int idClaseEnVivo;

    /** Docente curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idDocenteCurso", nullable = false)
    private DocenteCurso docenteCurso;

    /** Estado clase en vivo de este registro. */
    @ManyToOne
    @JoinColumn(name = "idEstadoClaseEnVivo", nullable = false)
    private EstadoClaseEnVivo estadoClaseEnVivo;

    /** Relación con Tema: el tema que se dicta en la clase. */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Relación con Grabacion: la grabación de la clase, cuando ya está disponible. */
    @ManyToOne
    @JoinColumn(name = "idGrabacion", nullable = true)
    private Grabacion grabacion;

    /** Cohorte de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    /** Título de la clase. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Fecha y hora en que se dicta la clase. */
    @Column(name = "fechaHora", nullable = false)
    private LocalDateTime fechaHora;

    /** Duración de la clase, en minutos. */
    @Column(name = "duracionMinutos", nullable = false)
    private int duracion;

    /** Fecha hasta la que se puede ver la grabación de la clase. */
    @Column(name = "fechaVencimientoGrabacion", nullable = true)
    private java.time.LocalDate fechaVencimientoGrabacion;

    /** URL del servidor RTMP para transmitir la clase. */
    @Column(name = "urlRtmp", nullable = true, length = 255)
    private String urlRtmp;

    /** Clave de transmisión de la clase. */
    @Column(name = "claveStream", nullable = true, length = 100, unique = true)
    private String claveStream;

    /** Indica si el elemento está oculto para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea la clase en vivo con los datos básicos. */
    public ClaseEnVivo(DocenteCurso docenteCurso, EstadoClaseEnVivo estadoClaseEnVivo, Tema tema, Cohorte cohorte, String titulo, LocalDateTime fechaHora, int duracion) {
        this.docenteCurso = docenteCurso;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.tema = tema;
        this.cohorte = cohorte;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.duracion = duracion;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la clase en vivo. */
    @Override
    public String toString() {
        return titulo;
    }
}
