package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el certificado de aprobación emitido a un alumno por una inscripción. */
@Entity
@Table(name = "Certificado")
@Check(name = "ck_certificado_dni", constraints = "dni_alumno ~ '^[0-9]{7,8}$'")
@Getter
@Setter
@NoArgsConstructor
public class Certificado {

    /** Identificador único del certificado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCertificado")
    private int idCertificado;

    /** Inscripcion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /** Número del certificado (formato CERT-AAAA-000000). */
    @Column(name = "numero", nullable = false, length = 100, unique = true)
    private String numero;

    /** Fecha y hora de emisión del certificado. */
    @Column(name = "fechaEmision", nullable = false)
    private LocalDateTime fechaEmision;

    /** Indica si el certificado fue enviado por correo electrónico al alumno. */
    @Column(name = "enviado", nullable = false)
    private Boolean enviado = false;

    /** Texto del certificado. */
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    /** Nombre completo del alumno al momento de la emisión. */
    @Column(name = "nombreAlumno", nullable = false, length = 100)
    private String nombreAlumno;

    /** DNI del alumno al momento de la emisión. */
    @Column(name = "dniAlumno", nullable = false, length = 8)
    private String dniAlumno;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el certificado fue anulado (por ejemplo, ante un fraude). */
    @Column(name = "anulado", nullable = false)
    private Boolean anulado = false;

    /** Crea el certificado con los datos básicos. */
    public Certificado(Inscripcion inscripcion, String numero, LocalDateTime fechaEmision, String texto, String nombreAlumno, String dniAlumno) {
        this.inscripcion = inscripcion;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.texto = texto;
        this.nombreAlumno = nombreAlumno;
        this.dniAlumno = dniAlumno;
    }

    /** Anula el certificado (por ejemplo, ante un fraude), estableciendo el atributo 'anulado' a true. */
    public void anular() {
        anulado = true;
    }

    /** Verifica si el certificado fue anulado. */
    public boolean estaAnulado() {
        return anulado;
    }

    /** Devuelve una representación en forma de cadena del certificado. */
    @Override
    public String toString() {
        return numero;
    }
}
