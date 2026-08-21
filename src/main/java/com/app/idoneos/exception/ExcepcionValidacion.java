package com.app.idoneos.exception;
import com.app.idoneos.service.Reportes.*;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de validación de reglas de negocio en precondiciones y datos (400 BAD REQUEST).
 */
public class ExcepcionValidacion extends ExcepcionNegocio {

    public ExcepcionValidacion(String mensaje) {
        super(mensaje, HttpStatus.BAD_REQUEST);
    }

    public ExcepcionValidacion(String mensaje, HttpStatus estadoHttp) {
        super(mensaje, estadoHttp);
    }
}

