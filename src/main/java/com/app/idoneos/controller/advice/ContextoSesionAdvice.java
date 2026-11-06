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

/**
 * Consejo (advice) de controladores con el contexto de la sesión que necesitan todas las pantallas: el usuario autenticado (por
 * ejemplo, para la barra de navegación) y el mensaje que se muestra una sola vez después de un inicio de sesión.
 * Spring lo aplica a todos los controladores, así que ninguno tiene que agregarlos al modelo por su cuenta.
 */
@ControllerAdvice
public class ContextoSesionAdvice {

    /** Nombre del atributo de sesión con un mensaje que se muestra una sola vez en la página siguiente. */
    public static final String MENSAJE_DE_SESION = "mensajeDeSesion";

    /** Nombre del atributo de sesión que marca que ya se le preguntó al alumno por sus intereses en esta sesión. */
    public static final String INTERESES_PREGUNTADOS = "interesesPreguntados";

    @Autowired
    private InteresAlumnoService interesAlumnoService;

    /**
     * Si hay un mensaje guardado en la sesión (por ejemplo, el de inicio de sesión exitoso), lo agrega al modelo
     * como "mensaje", para que se muestre una sola vez, y lo elimina de la sesión.
     *
     * @param request La solicitud HTTP.
     * @param model    El modelo de la vista.
     */
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

    /**
     * Para el alumno con sesión iniciada agrega lo que necesita la ventana de intereses del menú: las categorías que puede elegir, las
     * que ya eligió y si hay que preguntarle qué le interesa al ingresar (todavía no eligió ninguna, no llegó al máximo de «Ahora no» y
     * no se le preguntó ya en esta sesión). Solo en las páginas completas: no en las peticiones de filas ni en las de datos.
     *
     * @param authentication La autenticación actual, si existe.
     * @param request        La solicitud HTTP.
     * @param model          El modelo de la vista.
     */
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

    /**
     * Agrega el usuario autenticado al modelo de todas las vistas.
     *
     * @param authentication La autenticación actual, si existe.
     * @return El usuario autenticado, o {@code null} si no hay sesión iniciada.
     */
    @ModelAttribute("usuario")
    public Usuario agregarUsuario(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            return (Usuario) authentication.getPrincipal();
        }
        return null;
    }
}
