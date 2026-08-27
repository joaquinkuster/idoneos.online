package com.app.idoneos.exception;
import com.app.idoneos.service.modulo_reportes.*;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de negocio para el sistema Idóneos Online.
 * Encapsula el código de estado HTTP semántico y el mensaje descriptivo en español para el cliente.
 */
public class ExcepcionNegocio extends RuntimeException {

    private final HttpStatus estadoHttp;

    public ExcepcionNegocio(String mensaje) {
        super(mensaje);
        this.estadoHttp = HttpStatus.BAD_REQUEST;
    }

    public ExcepcionNegocio(String mensaje, HttpStatus estadoHttp) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
    }

    public HttpStatus getEstadoHttp() {
        return estadoHttp;
    }
}

