package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Certificado de aprobación de un curso.
 * Se emite al alumno cuando aprueba la autoevaluación final.
 * Relación 0..1 con Inscripcion (no toda inscripción tiene certificado).
 */
@Entity
@Table(name = "certificado")
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
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "ruta_archivo", nullable = true, length = 150)
    private String rutaArchivo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false, unique = true)
    private Inscripcion inscripcion;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public Inscripcion getInscripcion() { return inscripcion; }
    public void setInscripcion(Inscripcion inscripcion) { this.inscripcion = inscripcion; }

    public Certificado(String numero, Inscripcion inscripcion) {
        this.numero = numero;
        this.inscripcion = inscripcion;
    }
}
