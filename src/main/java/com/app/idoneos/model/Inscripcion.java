package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Vínculo entre un Usuario (alumno) y un Curso.
 * fechaVencimientoAcceso se calcula como fecha + Curso.mesesAcceso meses.
 */
@Entity
@Table(name = "inscripciones")
@Getter @Setter
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_descuento", nullable = true)
    private Descuento descuento;

    /**
     * DDL: fecha timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

    /**
     * Calculado: fecha + curso.mesesAcceso. Define hasta cuándo el alumno puede acceder.
     * Null si el curso no tiene límite de tiempo (mesesAcceso = null).
     * DDL: fecha_vencimiento_acceso timestamp — cambiado de LocalDate a LocalDateTime.
     */
    @Column(name = "fecha_vencimiento_acceso", nullable = true)
    private LocalDateTime fechaVencimientoAcceso;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Progreso> progresos = new HashSet<>();

    @OneToOne(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Certificado certificado;

    public Inscripcion(Usuario usuario, Curso curso) {
        this.usuario = usuario;
        this.curso = curso;
        if (curso.getMesesAcceso() != null) {
            this.fechaVencimientoAcceso = LocalDateTime.now().plusMonths(curso.getMesesAcceso());
        }
    }

    public boolean tieneAcceso() {
        if (baja) return false;
        if (fechaVencimientoAcceso == null) return true;
        return LocalDateTime.now().isBefore(fechaVencimientoAcceso);
    }
}
