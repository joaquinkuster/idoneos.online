package com.app.idoneos.controller.modulo_configuracion;

import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Configuracion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.modulo_configuracion.ConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * TRAZABILIDAD — Controller para el Módulo de Configuración del Sistema (MOD-NF-10).
 *
 * Mapea la pantalla de Configuración de Parámetros:
 *   CU-99 — Configurar parámetros del sistema → GET /configuracion, POST /configuracion/guardar, POST /configuracion/borrar/{id}
 */
@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    @Autowired private ConfiguracionRepository configRepo;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    /**
     * CU-99 — Configurar parámetros del sistema (listar).
     * Vista: cu-99-configurar-parametros.html
     */
    @GetMapping
    public String verConfiguracion(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Configuracion> parametros = configRepo.findAll();
        model.addAttribute("parametros", parametros);
        model.addAttribute("titulo", "CU-99 - Configurar parámetros del sistema | Idóneos Online");
        return "pages/configuracion/cu-99-configurar-parametros";
    }

    /**
     * CU-99 — Configurar parámetros del sistema (guardar/actualizar).
     */
    @PostMapping("/guardar")
    public String guardarParametro(@RequestParam String clave,
                                   @RequestParam String valor,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        if (clave == null || clave.trim().isEmpty() || valor == null || valor.trim().isEmpty()) {
            throw new ExcepcionValidacion("EX-CU99-01: La clave y el valor del parámetro son obligatorios.");
        }

        Usuario usuarioActual = (auth != null && auth.getPrincipal() instanceof Usuario) ? (Usuario) auth.getPrincipal() : null;
        Administrador adminActual = usuarioActual != null ? usuarioActual.getAdministrador() : null;

        Configuracion c = configRepo.findByClave(clave.trim())
                .orElseGet(() -> new Configuracion(clave.trim(), valor.trim(), adminActual));
        c.setValor(valor.trim());
        if (c.getAdministrador() == null && adminActual != null) {
            c.setAdministrador(adminActual);
        }
        configRepo.save(c);
        ra.addFlashAttribute("mensaje", "Parámetro '" + clave + "' guardado correctamente.");
        return "redirect:/configuracion";
    }

    /**
     * CU-99 — Configurar parámetros del sistema (eliminar).
     */
    @PostMapping("/borrar/{id}")
    public String borrarParametro(@PathVariable Integer id, RedirectAttributes ra) {
        configRepo.deleteById(id);
        ra.addFlashAttribute("mensaje", "Parámetro eliminado con éxito.");
        return "redirect:/configuracion";
    }
}
