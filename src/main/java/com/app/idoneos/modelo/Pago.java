package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pago realizado por una inscripción. Registra el monto, el método, el estado y los datos de la operación externa.
 */
@Entity
@Table(name = "Pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    /**
     * Identificador único del pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private int idPago;

    /**
     * Relación con EstadoPago.
     */
    @ManyToOne
    @JoinColumn(name = "idEstadoPago", nullable = false)
    private EstadoPago estadoPago;

    /**
     * Relación con MetodoPago.
     */
    @ManyToOne
    @JoinColumn(name = "idMetodoPago", nullable = false)
    private MetodoPago metodoPago;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Relación con Descuento. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idDescuento", nullable = true)
    private Descuento descuento;

    /**
     * Identificador de la solicitud de pago en el proveedor externo. Es opcional.
     */
    @Column(name = "idSolicitudPago", nullable = true, length = 50, unique = true)
    private String idSolicitudPago;

    /**
     * Identificador de la intención de pago en el proveedor externo.
     */
    @Column(name = "idIntencionExterna", nullable = false, length = 50, unique = true)
    private String idIntencionExterna;

    /**
     * Código de referencia del pago. Es opcional.
     */
    @Column(name = "codigoReferencia", nullable = true, length = 20, unique = true)
    private String codigoReferencia;

    /**
     * Monto del pago.
     */
    @Column(name = "monto", nullable = false)
    private float monto;

    /**
     * Últimos 4 dígitos de la tarjeta utilizada. Es opcional.
     */
    @Column(name = "ultimosDigitosTarjeta", nullable = true, length = 4)
    private String ultimosDigitosTarjeta;

    /**
     * Detalle del método de pago utilizado. Es opcional.
     */
    @Column(name = "detalleMetodoPago", nullable = true, columnDefinition = "TEXT")
    private String detalleMetodoPago;

    /**
     * Detalle del error ocurrido en la operación, si lo hubo. Es opcional.
     */
    @Column(name = "detalleErrorOperacion", nullable = true, columnDefinition = "TEXT")
    private String detalleErrorOperacion;

    /**
     * Fecha y hora de aprobación del pago. Es opcional.
     */
    @Column(name = "fechaAprobacion", nullable = true)
    private LocalDateTime fechaAprobacion;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Conjunto de comprobantes asociados.
     */
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    private Set<Comprobante> comprobantes = new HashSet<>();

    /**
     * Constructor para crear el pago con los datos básicos.
     *
     * @param estadoPago relación con EstadoPago.
     * @param metodoPago relación con MetodoPago.
     * @param inscripcion relación con Inscripcion.
     * @param idIntencionExterna identificador de la intención de pago en el proveedor externo.
     * @param monto monto del pago.
     */
    public Pago(EstadoPago estadoPago, MetodoPago metodoPago, Inscripcion inscripcion, String idIntencionExterna, float monto) {
        this.estadoPago = estadoPago;
        this.metodoPago = metodoPago;
        this.inscripcion = inscripcion;
        this.idIntencionExterna = idIntencionExterna;
        this.monto = monto;
    }

    /**
     * Devuelve una representación en forma de cadena del pago.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Pago #" + idPago;
    }
}
