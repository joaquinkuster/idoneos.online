package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un pago realizado por una inscripción. */
@Entity
@Table(name = "Pago")
@Check(name = "ck_pago_monto", constraints = "monto >= 0")
@Check(name = "ck_pago_ultimos_digitos", constraints = "ultimos_digitos_tarjeta is null or ultimos_digitos_tarjeta ~ '^[0-9]{1,4}$'")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    /** Identificador único del pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private int idPago;

    /** Estado pago de este registro. */
    @ManyToOne
    @JoinColumn(name = "idEstadoPago", nullable = false)
    private EstadoPago estadoPago;

    /** Metodo pago de este registro. */
    @ManyToOne
    @JoinColumn(name = "idMetodoPago", nullable = false)
    private MetodoPago metodoPago;

    /** Inscripcion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /** Descuento de este registro. */
    @ManyToOne
    @JoinColumn(name = "idDescuento", nullable = true)
    private Descuento descuento;

    /** Identificador de la solicitud de pago en el proveedor externo. */
    @Column(name = "idSolicitudPago", nullable = true, length = 50, unique = true)
    private String idSolicitudPago;

    /** Identificador de la intención de pago en el proveedor externo. */
    @Column(name = "idIntencionExterna", nullable = false, length = 50, unique = true)
    private String idIntencionExterna;

    /** Código de referencia del pago. */
    @Column(name = "codigoReferencia", nullable = true, length = 20, unique = true)
    private String codigoReferencia;

    /** Número de comprobante de la operación: el de la transferencia bancaria o el que muestra MODO. */
    @Column(name = "numeroComprobante", nullable = true, length = 100)
    private String numeroComprobante;

    /** Monto del pago. */
    @Column(name = "monto", nullable = false)
    private float monto;

    /** Últimos 4 dígitos de la tarjeta utilizada. */
    @Column(name = "ultimosDigitosTarjeta", nullable = true, length = 4)
    private String ultimosDigitosTarjeta;

    /** Detalle del método de pago utilizado. */
    @Column(name = "detalleMetodoPago", nullable = true, columnDefinition = "TEXT")
    private String detalleMetodoPago;

    /** Motivo del rechazo del pago, si fue rechazado. */
    @Column(name = "motivoRechazo", nullable = true, columnDefinition = "TEXT")
    private String motivoRechazo;

    /** Fecha y hora de aprobación del pago. */
    @Column(name = "fechaAprobacion", nullable = true)
    private LocalDateTime fechaAprobacion;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Comprobantes que referencian a este registro. */
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    private Set<Comprobante> comprobantes = new HashSet<>();

    /** Crea el pago con los datos básicos. */
    public Pago(EstadoPago estadoPago, MetodoPago metodoPago, Inscripcion inscripcion, String idIntencionExterna, float monto) {
        this.estadoPago = estadoPago;
        this.metodoPago = metodoPago;
        this.inscripcion = inscripcion;
        this.idIntencionExterna = idIntencionExterna;
        this.monto = monto;
    }

    /** Indica si el pago está aprobado. */
    public boolean estaAprobado() {
        return EstadoPago.APROBADO.equals(estadoPago.getNombre());
    }

    /** Indica si el pago está pendiente de confirmación. */
    public boolean estaPendiente() {
        return EstadoPago.PENDIENTE.equals(estadoPago.getNombre());
    }

    /** Indica si el pago fue rechazado. */
    public boolean estaRechazado() {
        return EstadoPago.RECHAZADO.equals(estadoPago.getNombre());
    }

    /** Indica si se paga online con MODO. */
    public boolean esDeModo() {
        return MetodoPago.MODO.equals(metodoPago.getNombre());
    }

    /** Obtiene el comprobante emitido por el pago (los pagos aprobados lo tienen). */
    public Comprobante getComprobanteVigente() {
        return comprobantes.stream().max(java.util.Comparator.comparing(Comprobante::getFechaEmision)
                .thenComparingInt(Comprobante::getIdComprobante)).orElse(null);
    }

    /** Obtiene el momento que le corresponde al pago en los listados: cuándo se aprobó o, si todavía no, cuándo se creó. */
    public LocalDateTime getFechaDelPago() {
        return fechaAprobacion != null ? fechaAprobacion : fechaCreacion;
    }

    /** Arma el detalle del medio de pago para mostrarlo: el método y, si lo hay, el detalle. */
    public String getMedioDePago() {
        StringBuilder texto = new StringBuilder(metodoPago.getNombre());
        if (detalleMetodoPago != null && !detalleMetodoPago.isBlank()) {
            texto.append(" · ").append(detalleMetodoPago);
        }
        if (ultimosDigitosTarjeta != null && !ultimosDigitosTarjeta.isBlank()) {
            texto.append(" ···· ").append(ultimosDigitosTarjeta);
        }
        return texto.toString();
    }

    /** Devuelve una representación en forma de cadena del pago. */
    @Override
    public String toString() {
        return "Pago #" + idPago;
    }
}
