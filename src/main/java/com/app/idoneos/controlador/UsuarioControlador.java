package com.app.idoneos.controlador;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Usuario.UsuarioServicioImpl;
import com.app.idoneos.utilidades.Utilidades;

/**
 * Controlador de las acciones del usuario autenticado sobre su propia cuenta.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicioImpl usuarioServicio;

    /**
     * Cambia el rol por defecto del usuario autenticado (el rol con el que trabaja). Responde en JSON con
     * la página a la que debe ir el navegador para mostrar la vista del nuevo rol.
     *
     * @param idRol Identificador del rol que pasa a ser el rol por defecto.
     * @param auth  La autenticación actual.
     * @return Una respuesta con el mensaje de éxito y el destino, o el mensaje de error.
     */
    @PostMapping("/cambiarRolPorDefecto")
    public ResponseEntity<Map<String, String>> cambiarRolPorDefecto(
            @RequestParam(value = "idRol", required = false) Integer idRol, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Rol rol = usuarioServicio.cambiarRolPorDefecto(usuario.getIdUsuario(), idRol);
            usuario.setRolPorDefecto(rol); // mantiene actualizado el usuario de la sesión
            String destino = usuario.esAdministradorActivo() ? "/curso/buscar"
                    : usuario.esAlumnoActivo() ? "/inscripcion/misCursos" : "/inicio";
            return ResponseEntity.ok(Map.of("mensaje", "Ahora trabajás con el rol " + rol.getNombre() + ".",
                    "destino", destino));
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }
}
