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
     * Avisa al docente que un administrador lo habilitó o lo suspendió para dictar clases.
     *
     * @param usuario Usuario del docente.
     * @param habilitado {@code true} si fue habilitado, {@code false} si fue suspendido.
     * @throws IllegalStateException Si el correo no pudo enviarse.
     */
    void enviarCambioHabilitacionDocente(Usuario usuario, boolean habilitado);
}
