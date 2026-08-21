package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Inscripcion: Registro de la matrícula de un Alumno en una Cohorte.
 * Mapea directamente a la tabla "Inscripcion" en base_datos.sql.
 */
@Entity
@Table(name = "Inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private int idInscripcion;

    /** Fecha y hora de la inscripción. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** Fecha de vencimiento del acceso a los materiales. */
    @Column(name = "fecha_vencimiento_acceso", nullable = false)
    private LocalDateTime fechaVencimientoAcceso;

    /** Observaciones adicionales sobre la inscripción. */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /** Número de certificado emitido (si aplica). */
    @Column(name = "numero_certificado", length = 100)
    private String numeroCertificado;

    /** Nombre completo del alumno al momento de emitir el certificado. */
    @Column(name = "nombre_alumno", length = 100)
    private String nombreAlumno;

    /** DNI del alumno al momento de emitir el certificado. */
    @Column(name = "dni_alumno", length = 8)
    private String dniAlumno;

    /** Texto del certificado generado. */
    @Column(name = "texto_certificado", columnDefinition = "text")
    private String textoCertificado;

    /** Fecha de emisión del certificado. */
    @Column(name = "fecha_emision_certificado")
    private LocalDateTime fechaEmisionCertificado;

    /** Indica si el certificado fue enviado al alumno. */
    @Column(name = "certificado_enviado", nullable = false)
    private boolean certificadoEnviado = false;

    /** Estado de baja lógica de la inscripción. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Cohorte a la que está inscripto el alumno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cohorte", nullable = false)
    private Cohorte cohorte;

    /** Alumno inscripto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    public Inscripcion() {
    }

    public Inscripcion(Cohorte cohorte, Alumno alumno) {
        this.cohorte = cohorte;
        this.alumno = alumno;
        this.fecha = LocalDateTime.now();
    }

    public int getId() {
        return idInscripcion;
    }

    public int getIdInscripcion() {
        return idInscripcion;
    }

    public void setId(int id) {
        this.idInscripcion = id;
    }

    public void setIdInscripcion(int idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFechaVencimientoAcceso() {
        return fechaVencimientoAcceso;
    }

    public void setFechaVencimientoAcceso(LocalDateTime fechaVencimientoAcceso) {
        this.fechaVencimientoAcceso = fechaVencimientoAcceso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNumeroCertificado() {
        return numeroCertificado;
    }

    public void setNumeroCertificado(String numeroCertificado) {
        this.numeroCertificado = numeroCertificado;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public String getDniAlumno() {
        return dniAlumno;
    }

    public void setDniAlumno(String dniAlumno) {
        this.dniAlumno = dniAlumno;
    }

    public String getTextoCertificado() {
        return textoCertificado;
    }

    public void setTextoCertificado(String textoCertificado) {
        this.textoCertificado = textoCertificado;
    }

    public LocalDateTime getFechaEmisionCertificado() {
        return fechaEmisionCertificado;
    }

    public void setFechaEmisionCertificado(LocalDateTime fechaEmisionCertificado) {
        this.fechaEmisionCertificado = fechaEmisionCertificado;
    }

    public boolean isCertificadoEnviado() {
        return certificadoEnviado;
    }

    public boolean getCertificadoEnviado() {
        return certificadoEnviado;
    }

    public void setCertificadoEnviado(boolean certificadoEnviado) {
        this.certificadoEnviado = certificadoEnviado;
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

    public Cohorte getCohorte() {
        return cohorte;
    }

    public void setCohorte(Cohorte cohorte) {
        this.cohorte = cohorte;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }
}
