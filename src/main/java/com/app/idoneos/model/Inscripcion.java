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
}
