package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscripcion")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @JoinColumn(name = "descuento_id")
    private Descuento descuento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictado_id", nullable = false)
    private Dictado dictado;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private List<Progreso> progresos = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public LocalDateTime getFechaVencimientoAcceso() { return fechaVencimientoAcceso; }
    public void setFechaVencimientoAcceso(LocalDateTime fechaVencimientoAcceso) { this.fechaVencimientoAcceso = fechaVencimientoAcceso; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String numeroCertificado) { this.numeroCertificado = numeroCertificado; }

    public LocalDateTime getFechaEmisionCertificado() { return fechaEmisionCertificado; }
    public void setFechaEmisionCertificado(LocalDateTime fechaEmisionCertificado) { this.fechaEmisionCertificado = fechaEmisionCertificado; }

    public boolean isCertificadoEnviado() { return certificadoEnviado; }
    public boolean getCertificadoEnviado() { return certificadoEnviado; }
    public void setCertificadoEnviado(boolean certificadoEnviado) { this.certificadoEnviado = certificadoEnviado; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Descuento getDescuento() { return descuento; }
    public void setDescuento(Descuento descuento) { this.descuento = descuento; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Dictado getDictado() { return dictado; }
    public void setDictado(Dictado dictado) { this.dictado = dictado; }

    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }

    public List<Progreso> getProgresos() { return progresos; }
    public void setProgresos(List<Progreso> progresos) { this.progresos = progresos; }

    public Usuario getUsuario() { return alumno != null ? alumno.getUsuario() : null; }
    public void setUsuario(Usuario u) { if (alumno == null) alumno = new Alumno(u); else alumno.setUsuario(u); }
    public Curso getCurso() { return dictado != null && dictado.getPrograma() != null ? dictado.getPrograma().getCurso() : null; }



    /** Constructor de compatibilidad para InscripcionServiceImpl. */
    public Inscripcion(Usuario usuario, Curso curso) {
        Alumno a = new Alumno(usuario);
        this.alumno = a;
        // dictado se asigna posteriormente vía setDictado()
    }

}
