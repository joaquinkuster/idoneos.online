package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Comprobante digital emitido para un pago aprobado.
 */
@Entity
@Table(name = "Comprobante")
@Getter
@Setter
@NoArgsConstructor
public class Comprobante {

    /**
     * Identificador único del comprobante.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComprobante")
    private int idComprobante;

    /**
     * Relación con Pago.
     */
    @ManyToOne
    @JoinColumn(name = "idPago", nullable = false)
    private Pago pago;

    /**
     * Número del comprobante. Debe ser único.
     */
    @Column(name = "numero", nullable = false, length = 100, unique = true)
    private String numero;

    /**
     * Fecha y hora de emisión del comprobante.
     */
    @Column(name = "fechaEmision", nullable = false)
    private LocalDateTime fechaEmision;

    /**
     * Indica si el comprobante fue enviado por correo electrónico al pagador.
     */
    @Column(name = "enviado", nullable = false)
    private Boolean enviado = false;

    /**
     * Nombre completo del pagador al momento de la emisión.
     */
    @Column(name = "nombrePagador", nullable = false, length = 100)
    private String nombrePagador;

    /**
     * DNI del pagador al momento de la emisión.
     */
    @Column(name = "dniPagador", nullable = false, length = 8)
    private String dniPagador;

    /**
     * Constructor para crear el comprobante con los datos básicos.
     *
     * @param pago relación con Pago.
     * @param numero número del comprobante. Debe ser único.
     * @param fechaEmision fecha y hora de emisión del comprobante.
     * @param nombrePagador nombre completo del pagador al momento de la emisión.
     * @param dniPagador DNI del pagador al momento de la emisión.
     */
    public Comprobante(Pago pago, String numero, LocalDateTime fechaEmision, String nombrePagador, String dniPagador) {
        this.pago = pago;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.nombrePagador = nombrePagador;
        this.dniPagador = dniPagador;
    }

    /**
     * Devuelve una representación en forma de cadena del comprobante.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return numero;
    }
}
