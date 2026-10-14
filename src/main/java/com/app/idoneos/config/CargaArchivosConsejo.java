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
     * Informa que el archivo supera el tamaño máximo que acepta el servidor.
     *
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> archivoDemasiadoGrande() {
        return ResponseEntity.badRequest().body(Map.of("error", "Error! El archivo supera el tamaño máximo permitido (500 MB)."));
    }
}
