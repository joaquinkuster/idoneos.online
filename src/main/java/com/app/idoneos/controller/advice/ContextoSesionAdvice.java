package com.app.idoneos.controller.advice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.interesalumno.InteresAlumnoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Consejo (advice) de controladores con el contexto de la sesión que necesitan todas las pantallas. */
@ControllerAdvice
public class ContextoSesionAdvice {

    /** Nombre del atributo de sesión con un mensaje que se muestra una sola vez en la página siguiente. */
    public static final String MENSAJE_DE_SESION = "mensajeDeSesion";

    /** Nombre del atributo de sesión que marca que ya se le preguntó al alumno por sus intereses en esta sesión. */
    public static final String INTERESES_PREGUNTADOS = "interesesPreguntados";

    @Autowired
    private InteresAlumnoService interesAlumnoService;

    /** Agrega el mensaje guardado en la sesión, si lo hay, por ejemplo el de inicio de sesión exitoso. */
    @ModelAttribute
    public void agregarMensajeDeSesion(HttpServletRequest request, Model model) {
        HttpSession sesion = request.getSession(false); // no crea sesiones para los visitantes
        Object mensaje = sesion == null ? null : sesion.getAttribute(MENSAJE_DE_SESION);
        if (mensaje != null) {
            sesion.removeAttribute(MENSAJE_DE_SESION);
            if (!model.containsAttribute("mensaje")) {
                model.addAttribute("mensaje", mensaje);
            }
        }
    }

    /** Agrega, para el alumno con sesión iniciada, lo que necesita la ventana de intereses del menú. */
    @ModelAttribute
    public void agregarIntereses(Authentication authentication, HttpServletRequest request, Model model) {
        if (!"GET".equals(request.getMethod()) || request.getHeader("X-Tabla") != null
                || !(authentication != null && authentication.getPrincipal() instanceof Usuario usuario)
                || !usuario.esAlumnoActivo() || usuario.getAlumno() == null) {
            return;
        }
        var alumno = usuario.getAlumno();
        var elegidas = interesAlumnoService.buscarCategorias(alumno);
        HttpSession sesion = request.getSession(false);
        boolean preguntar = interesAlumnoService.debePreguntar(alumno)
                && (sesion == null || sesion.getAttribute(INTERESES_PREGUNTADOS) == null);
        model.addAttribute("interesesCategorias", interesAlumnoService.buscarCategoriasElegibles());
        model.addAttribute("interesesElegidos", elegidas.stream().map(com.app.idoneos.model.Categoria::getIdCategoria).toList());
        model.addAttribute("preguntarIntereses", preguntar);
    }

    /** Agrega el usuario autenticado al modelo de todas las vistas. */
    @ModelAttribute("usuario")
    public Usuario agregarUsuario(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            return (Usuario) authentication.getPrincipal();
        }
        return null;
    }
}
