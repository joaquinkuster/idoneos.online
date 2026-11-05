package com.app.idoneos.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.model.Rol;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.usuario.UsuarioService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador de las acciones del usuario autenticado sobre su propia cuenta.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Cambia el rol por defecto del usuario autenticado (el rol con el que trabaja). Responde en JSON con
     * la página a la que debe ir el navegador para mostrar la vista del nuevo rol.
     *
     * @param idRol Identificador del rol que pasa a ser el rol por defecto.
     * @param authentication  La autenticación actual.
     * @return Una respuesta con el mensaje de éxito y el destino, o el mensaje de error.
     */
    @PostMapping("/cambiarRolPorDefecto")
    public ResponseEntity<Map<String, String>> cambiarRolPorDefecto(
            @RequestParam(value = "idRol", required = false) Integer idRol, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Rol rol = usuarioService.cambiarRolPorDefecto(usuario.getIdUsuario(), idRol);
            usuario.setRolPorDefecto(rol); // mantiene actualizado el usuario de la sesión
            String destino = usuario.esAdministradorActivo() ? "/curso/buscar"
                    : usuario.esAlumnoActivo() ? "/inscripcion/buscar" : "/inicio";
            return RespuestaFormularioUtil.armarRespuestaExitosa("Ahora trabajás con el rol " + rol.getNombre() + ".", destino);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }
}
