package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Comprobante simple de un pago (no factura electrónica — alcance PMV).
 * Relación 1:1 con Pago.
 */
@Entity
@Table(name = "comprobante")
@Getter @Setter
@NoArgsConstructor
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * VARCHAR porque el formato incluye guiones y ceros a la izquierda.
     */
    @Column(name = "numero", nullable = false, length = 100)
    private String numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "ruta_archivo", nullable = true, length = 150)
    private String rutaArchivo;

    /**
     * true si ya se envió el comprobante al alumno por email.
     */
    @Column(name = "enviado", nullable = false)
    private Boolean enviado = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", nullable = false, unique = true)
    private Pago pago;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public Boolean getEnviado() { return enviado; }
    public void setEnviado(Boolean enviado) { this.enviado = enviado; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public Comprobante(String numero, Pago pago) {
        this.numero = numero;
        this.pago = pago;
    }
}
