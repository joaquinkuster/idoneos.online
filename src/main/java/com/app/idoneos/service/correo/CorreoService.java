package com.app.idoneos.service.correo;

import com.app.idoneos.model.Usuario;

/** Servicio de correo electrónico. */
public interface CorreoService {

    // ----------------------------------------------------------------------
    // Envíos
    // ----------------------------------------------------------------------

    /**
     * Envía al usuario el enlace para validar su correo electrónico.
     *
     * @param usuario Usuario destinatario.
     * @param token Token de un solo uso con el que se arma el enlace.
     * @throws IllegalStateException Si el correo no pudo enviarse.
     */
    void enviarValidacionCuenta(Usuario usuario, String token);

    /**
     * Envía al usuario el enlace para crear una contraseña nueva.
     *
     * @param usuario Usuario destinatario.
     * @param token Token de un solo uso con el que se arma el enlace.
     * @throws IllegalStateException Si el correo no pudo enviarse.
     */
    void enviarRecuperacionContrasena(Usuario usuario, String token);

    /**
     * Envía al usuario creado por un administrador el enlace para que defina su contraseña.
     *
     * @param usuario Usuario destinatario.
     * @param token Token de un solo uso con el que se arma el enlace.
     * @throws IllegalStateException Si el correo no pudo enviarse.
     */
    void enviarDefinicionContrasena(Usuario usuario, String token);

    /**
     * Envía al usuario el correo de una notificación del sistema. A diferencia de los otros envíos, este espera a que el correo
     * salga, para que quien lo pide sepa si funcionó.
     *
     * @param usuario Usuario destinatario.
     * @param asunto Asunto del correo.
     * @param mensaje Texto del aviso.
     * @param url Dirección del sistema a la que lleva el botón del correo, o {@code null} si no lleva a ninguna.
     * @throws IllegalStateException Si el correo no pudo enviarse.
     */
    void enviarNotificacion(Usuario usuario, String asunto, String mensaje, String url);
}
