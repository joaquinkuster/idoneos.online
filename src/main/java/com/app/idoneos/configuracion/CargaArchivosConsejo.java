package com.app.idoneos.configuracion;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Consejo de controladores para la carga de archivos. El límite de tamaño de las subidas lo controla Spring
 * antes de llegar a los controladores, por lo que su error se atiende acá para mostrar un mensaje claro
 * dentro del formulario.
 */
@ControllerAdvice
public class CargaArchivosConsejo {

    /**
     * Informa que la imagen supera el tamaño máximo permitido.
     *
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> archivoDemasiadoGrande() {
        return ResponseEntity.badRequest().body(Map.of("error", "Error! La imagen no puede superar los 2 MB."));
    }
}
