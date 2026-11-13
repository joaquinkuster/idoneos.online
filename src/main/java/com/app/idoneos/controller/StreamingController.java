package com.app.idoneos.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService;
import com.app.idoneos.service.streaming.StreamingService;

/** Controlador con el que el servidor de medios consulta quién puede transmitir y quién puede mirar. */
@RestController
@RequestMapping("/streaming")
public class StreamingController {

    @Autowired
    private ClaseEnVivoService claseEnVivoService;

    @Autowired
    private StreamingService streamingService;

    /** POST /streaming/autorizar: Responde 200 si el pedido que recibió el servidor de medios está permitido, y 401 si no. */
    @PostMapping("/autorizar")
    public ResponseEntity<Void> autorizar(@RequestBody Map<String, Object> pedido) {
        String accion = texto(pedido, "action");
        String camino = texto(pedido, "path");
        boolean permitido = switch (accion) {
            // El servidor se consulta a sí mismo con la clave de la aplicación
            case "api" -> streamingService.esClaveDeApi(texto(pedido, "password"));
            // Transmite quien conoce la clave de la clase, y solo mientras la clase está en vivo
            case "publish" -> claseEnVivoService.buscarPorCamino(camino).filter(ClaseEnVivo::esEnVivo)
                    .filter(c -> c.getClaveStream() != null
                            && c.getClaveStream().equals(streamingService.resumirClave(texto(pedido, "password"))))
                    .isPresent();
            // Mira quien trae un acceso firmado para ese camino
            case "read" -> streamingService.validarAcceso(parametro(texto(pedido, "query"), "token"), camino);
            default -> false;
        };
        return ResponseEntity.status(permitido ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).build();
    }

    private static String texto(Map<String, Object> pedido, String campo) {
        Object valor = pedido.get(campo);
        return valor == null ? "" : valor.toString();
    }

    /** Obtiene el valor de un parámetro de una cadena de consulta. */
    private static String parametro(String consulta, String nombre) {
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            if (igual > 0 && par.substring(0, igual).equals(nombre)) {
                return par.substring(igual + 1);
            }
        }
        return null;
    }
}
