package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Certificado de aprobación de un curso.
 * Se emite al alumno cuando aprueba la autoevaluación final.
 * Relación 0..1 con Inscripcion (no toda inscripción tiene certificado).
 */
@Entity
@Table(name = "certificados")
@Getter @Setter
@NoArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * VARCHAR porque el formato incluye guiones y ceros a la izquierda.
     */
    @Column(name = "numero", nullable = false, unique = true, length = 100)
    private String numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision = LocalDate.now();

    @Column(name = "ruta_archivo", nullable = true, length = 150)
    private String rutaArchivo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false, unique = true)
    private Inscripcion inscripcion;

    public Certificado(String numero, Inscripcion inscripcion) {
        this.numero = numero;
        this.inscripcion = inscripcion;
    }
}
