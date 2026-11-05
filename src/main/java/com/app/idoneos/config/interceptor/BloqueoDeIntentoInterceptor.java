package com.app.idoneos.config.interceptor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.intentoautoevaluacion.IntentoAutoevaluacionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mantiene al alumno dentro del cuestionario (CU-70): mientras tiene un intento en curso de una autoevaluación que no
 * permite salir, cualquier pedido a otra sección de la plataforma lo redirige al intento. Solo dejan pasar el propio
 * cuestionario, los archivos estáticos y el cierre de sesión (que no termina el intento: el tiempo sigue corriendo y al
 * volver a ingresar se lo lleva de nuevo al cuestionario). El bloqueo lo hace el servidor, así que vale para cualquier
 * pestaña o dispositivo del mismo alumno. Cuando se agota el tiempo, el intento se cierra y el bloqueo se levanta.
 */
@Component
public class BloqueoDeIntentoInterceptor implements HandlerInterceptor {

    /** Rutas que se pueden usar durante un intento que no permite salir. */
    private static final List<String> PERMITIDAS = List.of("/intento/", "/logout", "/css/", "/js/", "/img/", "/uploads/",
            "/fonts/", "/webjars/", "/error", "/favicon");

    @Autowired
    private IntentoAutoevaluacionService intentoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (PERMITIDAS.stream().anyMatch(ruta::startsWith)) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario) || usuario.getAlumno() == null) {
            return true;
        }
        Optional<IntentoAutoevaluacion> intento = intentoService.buscarEnCursoSinSalida(usuario.getAlumno());
        if (intento.isEmpty()) {
            return true;
        }
        if ("GET".equals(request.getMethod())) {
            response.sendRedirect(request.getContextPath() + "/intento/realizar/" + intento.get().getIdIntentoAutoevaluacion());
        } else {
            response.sendError(HttpStatus.FORBIDDEN.value());
        }
        return false;
    }
}
