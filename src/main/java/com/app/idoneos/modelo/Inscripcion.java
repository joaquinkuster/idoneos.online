package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inscripción de un alumno a una cohorte. Define hasta cuándo tiene acceso al contenido.
 */
@Entity
@Table(name = "Inscripcion")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    /**
     * Identificador único de la inscripción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscripcion")
    private int idInscripcion;

    /**
     * Relación con Cohorte.
     */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    /**
     * Relación con Alumno.
     */
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /**
     * Fecha y hora en que vence el acceso al contenido.
     */
    @Column(name = "fechaVencimientoAcceso", nullable = false)
    private LocalDateTime fechaVencimientoAcceso;

    /**
     * Observaciones sobre la inscripción. Es opcional.
     */
    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

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
     * Indica si la inscripción está habilitada. Es opcional.
     */
    @Column(name = "habilitado", nullable = true)
    private Boolean habilitado;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Conjunto de progresos asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Progreso> progresos = new HashSet<>();

    /**
     * Conjunto de consultasForo asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<ConsultaForo> consultasForo = new HashSet<>();

    /**
     * Conjunto de pagos asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    /**
     * Conjunto de intentosAutoevaluacion asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<IntentoAutoevaluacion> intentosAutoevaluacion = new HashSet<>();

    /**
     * Conjunto de certificados asociados.
     */
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Set<Certificado> certificados = new HashSet<>();

    /**
     * Constructor para crear la inscripción con los datos básicos.
     *
     * @param cohorte relación con Cohorte.
     * @param alumno relación con Alumno.
     * @param fechaVencimientoAcceso fecha y hora en que vence el acceso al contenido.
     */
    public Inscripcion(Cohorte cohorte, Alumno alumno, LocalDateTime fechaVencimientoAcceso) {
        this.cohorte = cohorte;
        this.alumno = alumno;
        this.fechaVencimientoAcceso = fechaVencimientoAcceso;
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
     * Devuelve una representación en forma de cadena de la inscripción.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Inscripcion #" + idInscripcion;
    }
}
