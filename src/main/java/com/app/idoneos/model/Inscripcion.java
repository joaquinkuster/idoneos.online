package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Inscripción: vínculo entre un Alumno y un Dictado puntual de un
 * curso.
 * Los datos del certificado se guardan en la propia inscripción.
 */
@Entity
@Table(name = "Inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "fecha_vencimiento_acceso", nullable = false)
    private LocalDateTime fechaVencimientoAcceso;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "numero_certificado", length = 100)
    private String numeroCertificado;

    @Column(name = "fecha_emision_certificado")
    private LocalDateTime fechaEmisionCertificado;

    @Column(name = "certificado_enviado", nullable = false)
    private boolean certificadoEnviado = false;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictado_id", nullable = false)
    private Dictado dictado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descuento_id")
    private Descuento descuento;

    public Inscripcion() {
    }

    public Inscripcion(Alumno alumno, Dictado dictado) {
        this.alumno = alumno;
        this.dictado = dictado;
        this.fecha = LocalDateTime.now();
        this.fechaVencimientoAcceso = LocalDateTime.now().plusMonths(6);
    }

    public Inscripcion(Usuario usuario, Curso curso) {
        this.fecha = LocalDateTime.now();
        this.fechaVencimientoAcceso = LocalDateTime.now().plusMonths(6);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Dictado getDictado() {
        return dictado;
    }

    public void setDictado(Dictado dictado) {
        this.dictado = dictado;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public void setDescuento(Descuento descuento) {
        this.descuento = descuento;
    }

    public Curso getCurso() {
        if (dictado != null && dictado.getPrograma() != null) {
            return dictado.getPrograma().getCurso();
        }
        return null;
    }
}
