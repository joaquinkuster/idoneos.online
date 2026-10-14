package com.app.idoneos.servicio.Pago;

/**
 * Datos que necesita el navegador para que el alumno complete un pago online: el pago que se registró y la solicitud
 * creada en la plataforma (el QR y el enlace no se guardan; si hace falta se vuelven a pedir a la plataforma).
 *
 * @param idPago      El pago registrado, pendiente de confirmación.
 * @param idSolicitud El identificador de la solicitud en la plataforma.
 * @param qr          El código QR que se escanea desde la app de MODO o de un banco.
 * @param deeplink    El enlace que abre la app de pago desde un celular.
 * @param simulado    Si la plataforma es simulada (no hay modal de MODO).
 * @param scriptModal La dirección del script que muestra el modal de MODO (nula si es simulada).
 * @param aprobado    Si el pago ya quedó aprobado (por ejemplo, cuando el descuento cubre todo el precio).
 */
public record SolicitudDePago(int idPago, String idSolicitud, String qr, String deeplink, boolean simulado,
        String scriptModal, boolean aprobado) {
}
