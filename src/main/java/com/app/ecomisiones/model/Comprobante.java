package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Comprobante simple de un pago (no factura electrónica — alcance PMV).
 * Relación 1:1 con Pago.
 */
@Entity
@Table(name = "comprobantes")
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
    private LocalDate fechaEmision = LocalDate.now();

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

    public Comprobante(String numero, Pago pago) {
        this.numero = numero;
        this.pago = pago;
    }
}
