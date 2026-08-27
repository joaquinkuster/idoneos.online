package com.app.idoneos.exception;
import com.app.idoneos.service.modulo_reportes.*;

import org.springframework.http.HttpStatus;

/**
 * Excepción para conflictos de negocio o estado no permitido (409 CONFLICT).
 */
public class ExcepcionConflicto extends ExcepcionNegocio {

    public ExcepcionConflicto(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}

