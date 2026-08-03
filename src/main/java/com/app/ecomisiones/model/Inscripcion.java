package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha = LocalDate.now();

    @Column(name = "observaciones", nullable = true, length = 500)
    private String observaciones;

    /**
     * Calculado: fecha + curso.mesesAcceso. Define hasta cuándo el alumno puede acceder.
     * Null si el curso no tiene límite de tiempo (mesesAcceso = null).
     */
    @Column(name = "fecha_vencimiento_acceso", nullable = true)
    private LocalDate fechaVencimientoAcceso;

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
            this.fechaVencimientoAcceso = LocalDate.now().plusMonths(curso.getMesesAcceso());
        }
    }

    public boolean tieneAcceso() {
        if (baja) return false;
        if (fechaVencimientoAcceso == null) return true;
        return LocalDate.now().isBefore(fechaVencimientoAcceso) || LocalDate.now().isEqual(fechaVencimientoAcceso);
    }
}
