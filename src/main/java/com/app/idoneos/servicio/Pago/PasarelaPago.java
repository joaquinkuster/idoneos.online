package com.app.idoneos.servicio.Pago;

import java.time.LocalDateTime;

/**
 * Plataforma de pagos online (MODO). Hay dos implementaciones: {@link ModoPasarela}, que usa la API de MODO, y
 * {@link PasarelaSimulada}, para correr el sistema y las pruebas sin red ni celular.
 *
 * El sistema crea una solicitud de pago (payment request) con un identificador externo único, el alumno la paga desde
 * la app de MODO o de su banco y el resultado se conoce consultándolo (o porque MODO lo notifica por webhook).
 */
public interface PasarelaPago {

    /** Resultado de un pago en la plataforma. */
    enum Estado { PENDIENTE, APROBADO, RECHAZADO }

    /**
     * Solicitud de pago creada en la plataforma.
     *
     * @param id       Identificador de la solicitud en la plataforma.
     * @param qr       Código QR (texto) que el alumno escanea con la app de MODO o de su banco.
     * @param deeplink Enlace que abre la app de pago desde un celular.
     */
    record Solicitud(String id, String qr, String deeplink) {
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
     * Consulta el estado de una solicitud de pago.
     *
     * @param idSolicitud El identificador de la solicitud en la plataforma.
     * @return El estado y, según corresponda, los datos del pago.
     * @throws IllegalStateException Si no se pudo consultar a la plataforma.
     */
    Resultado consultarPago(String idSolicitud);

    /**
     * Indica si la plataforma es simulada.
     *
     * @return {@code true} si no se conecta con MODO.
     */
    boolean esSimulada();

    /**
     * Monto mínimo que admite la plataforma para crear una solicitud.
     *
     * @return El monto mínimo, en pesos.
     */
    float montoMinimo();

    /**
     * Dirección del script que muestra el modal de pago de MODO en el navegador.
     *
     * @return La URL del script, o {@code null} si la plataforma es simulada.
     */
    String scriptModal();
}
