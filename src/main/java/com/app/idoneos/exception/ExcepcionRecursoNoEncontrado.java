package com.app.idoneos.exception;
import com.app.idoneos.service.modulo_reportes.*;

import org.springframework.http.HttpStatus;

/**
 * Excepción para recursos no encontrados en el sistema (404 NOT FOUND).
 */
public class ExcepcionRecursoNoEncontrado extends ExcepcionNegocio {

    public ExcepcionRecursoNoEncontrado(String nombreRecurso, String nombreCampo, Object valorCampo) {
        super(String.format("%s no encontrado/a con %s: '%s'", nombreRecurso, nombreCampo, valorCampo), HttpStatus.NOT_FOUND);
    }

    public ExcepcionRecursoNoEncontrado(String mensaje) {
        super(mensaje, HttpStatus.NOT_FOUND);
    }
}

