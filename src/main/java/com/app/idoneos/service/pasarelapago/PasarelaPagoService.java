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

    /**
     * Cuánto dura una solicitud de pago desde que se crea, en segundos: el plazo de confirmación del sistema (entre 5 y 10 minutos) más un colchón
     * de 10 segundos, sin pasar de los 10 minutos que admite MODO. Es lo que se le envía a MODO como vencimiento: después de ese momento, MODO
     * no acepta el pago.
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
     * @param vencimiento    Hasta cuándo se puede pagar la solicitud, según MODO (nulo si no lo informó).
     */
    record Resultado(Estado estado, Solicitud solicitud, String medioPago, String ultimosDigitos, String nombrePagador,
            String dniPagador, String referencia, String motivoError, LocalDateTime fechaPago, LocalDateTime vencimiento) {

        /** Resultado sin el vencimiento (el que informa MODO solo en la consulta de una solicitud). */
        public Resultado(Estado estado, Solicitud solicitud, String medioPago, String ultimosDigitos, String nombrePagador,
                String dniPagador, String referencia, String motivoError, LocalDateTime fechaPago) {
            this(estado, solicitud, medioPago, ultimosDigitos, nombrePagador, dniPagador, referencia, motivoError, fechaPago, null);
        }

        /** Resultado de una solicitud que todavía no se pagó ni se rechazó. */
        public static Resultado pendiente(Solicitud solicitud) {
            return pendiente(solicitud, null);
        }

        /** Resultado de una solicitud que todavía no se pagó ni se rechazó, con su vencimiento. */
        public static Resultado pendiente(Solicitud solicitud, LocalDateTime vencimiento) {
            return new Resultado(Estado.PENDIENTE, solicitud, null, null, null, null, null, null, null, vencimiento);
        }

        /** Resultado de una solicitud cuyo pago MODO está procesando. */
        public static Resultado procesando(Solicitud solicitud, LocalDateTime vencimiento) {
            return new Resultado(Estado.PROCESANDO, solicitud, null, null, null, null, null, null, null, vencimiento);
        }
    }
}
