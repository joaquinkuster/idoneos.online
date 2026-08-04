package com.app.idoneos.controller;

import com.app.idoneos.model.Configuracion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.ConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para la gestión de Parámetros Operativos del sistema (clave-valor).
 */
@Controller
@RequestMapping("/admin/configuracion")
public class ConfiguracionController {

    @Autowired private ConfiguracionRepository configRepo;

    @GetMapping
    public String verConfiguracion(Model model, Authentication auth) {
        List<Configuracion> parametros = configRepo.findAll();
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("parametros", parametros);
        model.addAttribute("titulo", "Configuración de Parámetros | Idóneos Online");
        return "pages/admin/configuracion";
    }

    @PostMapping("/guardar")
    public String guardarParametro(@RequestParam String clave,
                                   @RequestParam String valor,
                                   RedirectAttributes ra) {
        Configuracion c = configRepo.findByClave(clave)
                .orElseGet(() -> new Configuracion(clave, valor));
        c.setValor(valor);
        configRepo.save(c);
        ra.addFlashAttribute("mensaje", "Parámetro '" + clave + "' guardado correctamente.");
        return "redirect:/admin/configuracion";
    }

    @PostMapping("/borrar/{id}")
    public String borrarParametro(@PathVariable Integer id, RedirectAttributes ra) {
        configRepo.deleteById(id);
        ra.addFlashAttribute("mensaje", "Parámetro eliminado.");
        return "redirect:/admin/configuracion";
    }
}
