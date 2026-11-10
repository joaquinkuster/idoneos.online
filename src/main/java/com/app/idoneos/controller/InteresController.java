package com.app.idoneos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.controller.advice.ContextoSesionAdvice;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.interesalumno.InteresAlumnoService;
import com.app.idoneos.util.RespuestaFormularioUtil;

import jakarta.servlet.http.HttpSession;

/** Controlador del interés. */
@Controller
@RequestMapping("/interes")
public class InteresController {

    @Autowired
    private InteresAlumnoService interesAlumnoService;

    /** POST /interes/guardar: Guarda las categorías que le interesan al alumno (reemplaza las anteriores). */
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, String>> guardar(
            @RequestParam(value = "categoriaIds", required = false) List<Integer> categoriaIds, Authentication authentication,
            HttpSession sesion) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            boolean teniaIntereses = !interesAlumnoService.buscarCategorias(usuario.getAlumno()).isEmpty();
            interesAlumnoService.guardar(usuario.getAlumno().getIdAlumno(), categoriaIds);
            sesion.setAttribute(ContextoSesionAdvice.INTERESES_PREGUNTADOS, true);
            return RespuestaFormularioUtil.armarRespuestaExitosa(teniaIntereses ? "Intereses modificados correctamente."
                    : "Intereses registrados correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /interes/omitir: El alumno cerró la ventana sin guardar al preguntarle qué le interesa. */
    @PostMapping("/omitir")
    public ResponseEntity<Map<String, String>> omitir(HttpSession sesion) {
        try {
            sesion.setAttribute(ContextoSesionAdvice.INTERESES_PREGUNTADOS, true); // no se vuelve a preguntar en esta sesión
            return RespuestaFormularioUtil.armarRespuestaExitosa("Podés registrar tus intereses cuando quieras desde tu menú.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }
}
