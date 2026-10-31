package com.app.idoneos.controlador;

import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Vista Diseño de curso: el recorrido del diseño de un curso (datos del curso, temas, programa vigente, contenido y
 * cohortes que esperan ese diseño), con el siguiente paso sugerido. Desde ella el docente edita los datos del curso.
 */
@Controller
@RequestMapping("/diseno")
public class DisenoCursoControlador extends PanelCursoControlador {

    @Autowired
    private NivelServicioImpl nivelServicio;

    /**
     * Muestra el estado del diseño del curso.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista Diseño de curso.
     */
    @GetMapping("/buscar")
    public String ver(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            cargarContexto(modelo, contexto, "diseno");
            modelo.addAttribute("niveles", nivelServicio.obtenerTodo());
            modelo.addAttribute("puedeEditar", contexto.esDocente());
            modelo.addAttribute("titulo", "Diseño de curso | Idóneos Online");
            return "pages/diseno/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }
}
