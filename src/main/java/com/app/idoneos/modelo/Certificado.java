package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Certificado de aprobación emitido a un alumno por una inscripción. Puede anularse (por ejemplo, ante un fraude) o corregirse a pedido del titular.
 */
@Entity
@Table(name = "Certificado")
@Getter
@Setter
@NoArgsConstructor
public class Certificado {

    /**
     * Identificador único del certificado.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCertificado")
    private int idCertificado;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Número del certificado (formato CERT-AAAA-000000). Debe ser único.
     */
    @Column(name = "numero", nullable = false, length = 100, unique = true)
    private String numero;

    /**
     * Fecha y hora de emisión del certificado.
     */
    @Column(name = "fechaEmision", nullable = false)
    private LocalDateTime fechaEmision;

    /**
     * Indica si el certificado fue enviado por correo electrónico al alumno.
     */
    @Column(name = "enviado", nullable = false)
    private Boolean enviado = false;

    /**
     * Texto del certificado.
     */
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    /**
     * Nombre completo del alumno al momento de la emisión.
     */
    @Column(name = "nombreAlumno", nullable = false, length = 100)
    private String nombreAlumno;

    /**
     * DNI del alumno al momento de la emisión.
     */
    @Column(name = "dniAlumno", nullable = false, length = 8)
    private String dniAlumno;

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
     * Indica si el certificado fue anulado (por ejemplo, ante un fraude). El valor predeterminado es "false".
     */
    @Column(name = "anulado", nullable = false)
    private Boolean anulado = false;

    /**
     * Constructor para crear el certificado con los datos básicos.
     *
     * @param inscripcion relación con Inscripcion.
     * @param numero número del certificado (formato CERT-AAAA-000000). Debe ser único.
     * @param fechaEmision fecha y hora de emisión del certificado.
     * @param texto texto del certificado.
     * @param nombreAlumno nombre completo del alumno al momento de la emisión.
     * @param dniAlumno DNI del alumno al momento de la emisión.
     */
    public Certificado(Inscripcion inscripcion, String numero, LocalDateTime fechaEmision, String texto, String nombreAlumno, String dniAlumno) {
        this.inscripcion = inscripcion;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.texto = texto;
        this.nombreAlumno = nombreAlumno;
        this.dniAlumno = dniAlumno;
    }

    /**
     * Anula el certificado (por ejemplo, ante un fraude), estableciendo el atributo 'anulado' a true.
     */
    public void anular() {
        anulado = true;
    }

    /**
     * Verifica si el certificado fue anulado.
     *
     * @return true si está anulado (anulado = true), false si está vigente.
     */
    public boolean estaAnulado() {
        return anulado;
    }

    /**
     * Devuelve una representación en forma de cadena del certificado.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return numero;
    }
}
