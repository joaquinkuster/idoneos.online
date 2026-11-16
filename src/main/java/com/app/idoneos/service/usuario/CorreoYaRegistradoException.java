package com.app.idoneos.service.usuario;

/** Excepción de correo ya registrado. Se lanza cuando alguien intenta ingresar con Google con un correo que ya tiene una cuenta propia. */
public class CorreoYaRegistradoException extends IllegalArgumentException {

    /** Crea la excepción con el mensaje que se muestra al usuario. */
    public CorreoYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
