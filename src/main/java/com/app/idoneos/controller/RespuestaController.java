package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.app.idoneos.model.RespuestaForo;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.respuestaforo.RespuestaForoService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador de las respuestas del foro (MOD-F-02): CU-40 Registrar respuesta (POST /respuesta/registrar), CU-41 Modificar
 * respuesta (GET y POST /respuesta/modificar/{id}) y CU-42 Dar de baja respuesta (POST /respuesta/darDeBaja/{id}).
 */
@Controller
@RequestMapping("/respuesta")
public class RespuestaController {

    @Autowired
    private PanelCursoComponent panel;


    @Autowired
    private RespuestaForoService respuestaService;

    /**
     * CU-40: Registra la respuesta de un docente a una consulta. Responde en JSON.
     *
     * @param consultaId Identificador de la consulta.
     * @param texto      El texto de la respuesta.
     * @param authentication       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "consultaId", required = false) Integer consultaId,
            @RequestParam(value = "texto", required = false) String texto, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            respuestaService.registrar(usuario.getDocente(), consultaId, texto);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Respuesta registrada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * CU-41: Modifica una respuesta propia, dentro del plazo de edición. Responde en JSON.
     *
     * @param id    Identificador de la respuesta.
     * @param texto El texto nuevo.
     * @param authentication  La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "texto", required = false) String texto, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            respuestaService.modificar(id, usuario.getDocente(), texto);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Respuesta modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * CU-42: Da de baja una respuesta (administrador). Responde en JSON.
     *
     * @param id Identificador de la respuesta.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            respuestaService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Respuesta dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Ventana de modificación a pedido

    /**
     * Ventana de modificación de una respuesta del foro, que la consulta pide al abrirla (en lugar de dibujar una por respuesta).
     *
     * @param id     La respuesta.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        RespuestaForo r = respuestaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        panel.cargarContextoDelForo(r.getConsulta().getTema(), (Usuario) authentication.getPrincipal(), model);
        model.addAttribute("r", r);
        return "pages/respuesta/modificar :: modal(r=${r})";
    }
}
