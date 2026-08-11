package com.app.idoneos.controller;

import com.app.idoneos.exception.ExcepcionValidacion;
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
 * Controlar para la gestión de parámetros operativos del sistema (CU-92: Configurar parámetros).
 * Permite listar, crear, modificar y eliminar la configuración clave-valor del sistema.
 */
@Controller
@RequestMapping("/admin/configuracion")
public class ConfiguracionController {

    @Autowired private ConfiguracionRepository configRepo;

    /**
     * CU-92 — Listar parámetros de configuración.
     */
    @GetMapping
    public String verConfiguracion(Model model, Authentication auth) {
        List<Configuracion> parametros = configRepo.findAll();
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("parametros", parametros);
        model.addAttribute("titulo", "Configuración de Parámetros | Idóneos Online");
        return "pages/admin/configuracion";
    }

    /**
     * CU-92 — Registrar o modificar valor de un parámetro.
     * Regla de negocio: La clave y el valor son obligatorios (Excepción CU-92, paso 4).
     */
    @PostMapping("/guardar")
    public String guardarParametro(@RequestParam String clave,
                                   @RequestParam String valor,
                                   RedirectAttributes ra) {
        if (clave == null || clave.trim().isEmpty() || valor == null || valor.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-92 Excepción paso 4: La clave y el valor del parámetro son obligatorios.");
        }

        Configuracion c = configRepo.findByClave(clave.trim())
                .orElseGet(() -> new Configuracion(clave.trim(), valor.trim()));
        c.setValor(valor.trim());
        configRepo.save(c);
        ra.addFlashAttribute("mensaje", "Parámetro '" + clave + "' guardado correctamente.");
        return "redirect:/admin/configuracion";
    }

    /**
     * CU-92 — Eliminar parámetro de configuración.
     */
    @PostMapping("/borrar/{id}")
    public String borrarParametro(@PathVariable Integer id, RedirectAttributes ra) {
        configRepo.deleteById(id);
        ra.addFlashAttribute("mensaje", "Parámetro eliminado.");
        return "redirect:/admin/configuracion";
    }
}
