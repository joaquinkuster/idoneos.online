package com.app.idoneos.controlador;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador de la página de error. Reemplaza al controlador de errores por defecto de Spring Boot
 * y se encarga de mostrar un mensaje claro cuando una ruta no existe, no se tiene acceso a ella
 * o ocurre un fallo inesperado.
 */
@Controller
public class ErrorControlador implements ErrorController {

    /**
     * Muestra la página de error con un mensaje según el código de estado HTTP.
     *
     * @param request La petición que originó el error.
     * @param modelo  El modelo de la vista.
     * @return La vista de error.
     */
    @RequestMapping("/error")
    public String verError(HttpServletRequest request, Model modelo) {
        Object estado = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int codigo = estado != null ? Integer.parseInt(estado.toString()) : 404;
        String encabezado;
        String detalle;
        if (codigo == 403) {
            encabezado = "Acceso denegado";
            detalle = "No tenés permisos para acceder a esta página con tu usuario.";
        } else if (codigo >= 500) {
            encabezado = "Ocurrió un error inesperado";
            detalle = "Tuvimos un problema al procesar tu solicitud. Intentá nuevamente en unos minutos.";
        } else {
            encabezado = "Página no encontrada";
            detalle = "La página que buscás no existe o fue movida.";
        }
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("encabezado", encabezado);
        modelo.addAttribute("detalle", detalle);
        modelo.addAttribute("titulo", encabezado + " | Idóneos Online");
        return "pages/error";
    }
}
