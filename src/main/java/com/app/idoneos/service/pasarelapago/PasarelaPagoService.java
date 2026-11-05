package com.app.idoneos.service.pasarelapago;

import com.app.idoneos.controller.PagoController;
import com.app.idoneos.model.Enlace;
import java.time.LocalDateTime;

/**
 * Plataforma de pagos online (MODO). {@link PasarelaPagoModoServiceImpl} la implementa con la API de MODO; las pruebas la reemplazan
 * por un mock, porque el pago real necesita red, credenciales y un celular.
 *
 * El sistema crea una solicitud de pago (payment request) con un identificador externo único, el alumno la paga desde
 * la app de MODO o de su banco y el resultado se conoce consultándolo (o porque MODO lo notifica por webhook).
 */
public interface PasarelaPagoService {

    /**
     * Ruta de este sistema a la que MODO avisa los cambios de estado de un pago (la atiende {@code PagoController}).
     * Si se configura la dirección pública del sistema, se envía en cada solicitud de pago para que MODO la use.
     */
    String RUTA_WEBHOOK = "/pago/webhook";

    // ---------------------------------------------------------------- Lecturas

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
     * Arma la dirección del sitio de pagos de MODO a la que se redirige al comprador para pagar una solicitud. MODO lo
     * devuelve a una de las dos direcciones de retorno cuando termina.
     *
     * @param idSolicitud Identificador de la solicitud de pago (de un solo uso: cada intento necesita una nueva).
     * @param urlExito    Dirección de la tienda a la que vuelve el comprador si el pago se aprueba.
     * @param urlError    Dirección de la tienda a la que vuelve si el pago se rechaza, falla o lo abandona.
     * @return La dirección del sitio de pagos, con los tres parámetros.
     */
    String urlCheckout(String idSolicitud, String urlExito, String urlError);

    // ---------------------------------------------------------------- Creaciones

    /**
     * Crea una solicitud de pago.
     *
     * @param idExterno   Identificador único de la intención de pago (cambia en cada solicitud).
     * @param monto       Monto a pagar, en pesos.
     * @param descripcion Descripción de la operación (máximo 100 caracteres).
     * @param nombre      Nombre y apellido del pagador.
     * @param correo      Correo del pagador.
     * @param dni         DNI del pagador.
     * @return La solicitud creada.
     * @throws IllegalArgumentException Si no se pudo crear la solicitud.
     */
    Solicitud crearSolicitud(String idExterno, float monto, String descripcion, String nombre, String correo, String dni);

    /** Resultado de un pago en la plataforma. */
    enum Estado { PENDIENTE, APROBADO, RECHAZADO }

    /**
     * Solicitud de pago creada en la plataforma.
     *
     * @param id Identificador de la solicitud en la plataforma.
     */
    record Solicitud(String id) {
    }

    /**
     * Estado de una solicitud de pago y, si ya se pagó o rechazó, los datos que informó la plataforma.
     *
     * @param estado         El estado de la solicitud.
     * @param solicitud      La solicitud (con su QR), mientras está pendiente.
     * @param medioPago      Tipo de medio de pago, por ejemplo "Tarjeta de crédito Visa".
     * @param ultimosDigitos Últimos cuatro dígitos de la tarjeta o cuenta.
     * @param nombrePagador  Nombre del pagador.
     * @param dniPagador     DNI del pagador.
     * @param referencia     Código de referencia de la operación.
     * @param motivoError    Motivo del rechazo, si lo hubo.
     * @param fechaPago      Cuándo se pagó, si se pagó.
     */
    record Resultado(Estado estado, Solicitud solicitud, String medioPago, String ultimosDigitos, String nombrePagador,
            String dniPagador, String referencia, String motivoError, LocalDateTime fechaPago) {

        /** Resultado de una solicitud que todavía no se pagó ni se rechazó. */
        public static Resultado pendiente(Solicitud solicitud) {
            return new Resultado(Estado.PENDIENTE, solicitud, null, null, null, null, null, null, null);
        }
    }
}
