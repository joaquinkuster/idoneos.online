package com.app.idoneos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST del sistema Idóneos Online.
 * Intercepta excepciones de negocio y validación devolviendo respuestas estructuradas en español.
 */
@RestControllerAdvice
public class ManejadorExcepcionesGlobales {

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionNegocio(ExcepcionNegocio ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("fechaHora", LocalDateTime.now());
        cuerpo.put("codigoEstado", ex.getEstadoHttp().value());
        cuerpo.put("error", ex.getEstadoHttp().getReasonPhrase());
        cuerpo.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(cuerpo, ex.getEstadoHttp());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionCampos(MethodArgumentNotValidException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("fechaHora", LocalDateTime.now());
        cuerpo.put("codigoEstado", HttpStatus.BAD_REQUEST.value());
        cuerpo.put("error", "Error de Validación de Campos");

        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(error.getField(), error.getDefaultMessage());
        }
        cuerpo.put("errores", erroresCampos);
        return new ResponseEntity<>(cuerpo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGenerica(Exception ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("fechaHora", LocalDateTime.now());
        cuerpo.put("codigoEstado", HttpStatus.INTERNAL_SERVER_ERROR.value());
        cuerpo.put("error", "Error Interno del Servidor");
        cuerpo.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(cuerpo, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
