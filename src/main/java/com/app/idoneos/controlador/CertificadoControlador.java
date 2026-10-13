package com.app.idoneos.controlador;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.servicio.Certificado.CertificadoServicioImpl;
import com.app.idoneos.utilidades.RespuestaUtilidad;

/**
 * Controlador de los certificados (MOD-F-03), solo para el Administrador.
 *
 * Mapea CU-48 Corregir datos personales del certificado: la ventana se abre desde la búsqueda de inscripciones
 * (CU-44) y este controlador recibe la corrección.
 */
@Controller
@RequestMapping("/certificado")
public class CertificadoControlador {

    @Autowired
    private CertificadoServicioImpl certificadoServicio;

    /**
     * CU-48: Corrige el nombre y/o el DNI del alumno registrados en un certificado emitido. Responde en JSON.
     *
     * @param id     Identificador del certificado.
     * @param nombre El nombre completo del alumno.
     * @param dni    El DNI del alumno.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/corregir/{id}")
    public ResponseEntity<Map<String, String>> corregirDatos(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "dni", required = false) String dni) {
        try {
            certificadoServicio.corregirDatos(id, nombre, dni);
            return RespuestaUtilidad.respuestaExitosa("Datos del certificado actualizados con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }
}
