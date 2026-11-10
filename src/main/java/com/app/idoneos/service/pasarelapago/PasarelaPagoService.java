package com.app.idoneos.service.pasarelapago;

import com.app.idoneos.controller.PagoController;
import com.app.idoneos.model.Enlace;
import java.time.LocalDateTime;

/** Servicio de la plataforma de pagos online. */
public interface PasarelaPagoService {

    /** Ruta de este sistema a la que MODO avisa los cambios de estado de un pago (la atiende PagoController). */
    String RUTA_WEBHOOK = "/pago/webhook";

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Consulta el estado de una solicitud de pago.
     *
     * @param idSolicitud El identificador de la solicitud en la plataforma.
     * @return El estado y, según corresponda, los datos del pago.
     * @throws IllegalStateException Si no se pudo consultar a la plataforma.
     */
    Resultado consultarPago(String idSolicitud);

    /**
     * Monto mínimo que admite la plataforma para crear una solicitud.
     *
     * @return El monto mínimo, en pesos.
     */
    float montoMinimo();

    /**
     * Arma la dirección del sitio de pagos de MODO a la que se redirige al comprador para pagar una solicitud.
     *
     * @param idSolicitud Identificador de la solicitud de pago (de un solo uso: cada intento necesita una nueva).
     * @param urlExito Dirección de la tienda a la que vuelve el comprador si el pago se aprueba.
     * @param urlError Dirección de la tienda a la que vuelve si el pago se rechaza, falla o lo abandona.
     * @return La dirección del sitio de pagos, con los tres parámetros.
     */
    String urlCheckout(String idSolicitud, String urlExito, String urlError);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Crea una solicitud de pago.
     *
     * @param idExterno Identificador único de la intención de pago (cambia en cada solicitud).
     * @param monto Monto a pagar, en pesos.
     * @param descripcion Descripción de la operación (máximo 100 caracteres).
     * @param nombre Nombre y apellido del pagador.
     * @param correo Correo del pagador.
     * @param dni DNI del pagador.
     * @return La solicitud creada.
     * @throws IllegalArgumentException Si no se pudo crear la solicitud.
     */
    Solicitud crearSolicitud(String idExterno, float monto, String descripcion, String nombre, String correo, String dni);

    /**
     * Cuánto dura una solicitud de pago desde que se crea, en segundos.
     *
     * @return La vigencia de una solicitud, en segundos.
     */
    int vigenciaSolicitudSegundos();

    /** Resultado de un pago en la plataforma. */
    enum Estado {
        /** MODO la creó o alguien escaneó su QR, pero todavía no se está pagando (Created o Scanned). */
        PENDIENTE,
        /** El pago está en vuelo: MODO lo está procesando (Processing) y puede terminar aprobado o rechazado. */
        PROCESANDO,
        APROBADO,
        RECHAZADO
    }

    /** Solicitud de pago creada en la plataforma. */
    record Solicitud(String id) {
    }

    /**
     * Estado de una solicitud de pago y, si ya se pagó o rechazó, los datos que informó la plataforma.
     *
     * @param estado El estado de la solicitud.
     * @param solicitud La solicitud (con su QR), mientras está pendiente.
     * @param medioPago Tipo de medio de pago, por ejemplo "Tarjeta de crédito Visa".
     * @param ultimosDigitos Últimos cuatro dígitos de la tarjeta o cuenta.
     * @param nombrePagador Nombre del pagador.
     * @param dniPagador DNI del pagador.
     * @param referencia Código de referencia de la operación.
     * @param motivoError Motivo del rechazo, si lo hubo.
     * @param fechaPago Cuándo se pagó, si se pagó.
     * @param vencimiento Hasta cuándo se puede pagar la solicitud, según MODO (nulo si no lo informó).
     */
    record Resultado(Estado estado, Solicitud solicitud, String medioPago, String ultimosDigitos, String nombrePagador,
            String dniPagador, String referencia, String motivoError, LocalDateTime fechaPago, LocalDateTime vencimiento) {

        /** Crea un resultado sin el vencimiento, que MODO informa solo en la consulta de una solicitud. */
        public Resultado(Estado estado, Solicitud solicitud, String medioPago, String ultimosDigitos, String nombrePagador,
                String dniPagador, String referencia, String motivoError, LocalDateTime fechaPago) {
            this(estado, solicitud, medioPago, ultimosDigitos, nombrePagador, dniPagador, referencia, motivoError, fechaPago, null);
        }

        /** Crea el resultado de una solicitud que todavía no se pagó ni se rechazó. */
        public static Resultado pendiente(Solicitud solicitud) {
            return pendiente(solicitud, null);
        }

        /** Crea el resultado de una solicitud que todavía no se pagó ni se rechazó, con su vencimiento. */
        public static Resultado pendiente(Solicitud solicitud, LocalDateTime vencimiento) {
            return new Resultado(Estado.PENDIENTE, solicitud, null, null, null, null, null, null, null, vencimiento);
        }

        /** Crea el resultado de una solicitud cuyo pago MODO está procesando. */
        public static Resultado procesando(Solicitud solicitud, LocalDateTime vencimiento) {
            return new Resultado(Estado.PROCESANDO, solicitud, null, null, null, null, null, null, null, vencimiento);
        }
    }
}
