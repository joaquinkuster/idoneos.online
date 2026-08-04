package com.app.idoneos.controller;

import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.DescuentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Controller para la Gestión de Descuentos (CU-28, CU-29, CU-30, CU-31).
 * Permite a la Administración dar de alta, modificar, dar de baja y consultar los descuentos aplicables.
 */
@Controller
@RequestMapping("/admin/descuentos")
public class DescuentoController {

    @Autowired
    private DescuentoRepository descuentoRepo;

    @GetMapping
    public String listarDescuentos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("descuentos", descuentoRepo.findAll());
        model.addAttribute("titulo", "Gestión de Descuentos | Idóneos Online");
        return "pages/admin/descuentos";
    }

    @PostMapping("/crear")
    public String crearDescuento(@RequestParam String nombre,
                                 @RequestParam Double porcentaje,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime vigenciaDesde,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime vigenciaHasta,
                                 @RequestParam(required = false) Integer cantidadLimite,
                                 @RequestParam(defaultValue = "0") Integer cursosRequeridos,
                                 RedirectAttributes ra) {
        Descuento d = new Descuento();
        d.setNombre(nombre);
        d.setPorcentaje(porcentaje);
        d.setVigenciaDesde(vigenciaDesde);
        d.setVigenciaHasta(vigenciaHasta);
        d.setCantidadLimite(cantidadLimite);
        d.setCursosRequeridos(cursosRequeridos);
        d.setFechaCreacion(LocalDateTime.now());

        descuentoRepo.save(d);
        ra.addFlashAttribute("mensaje", "Descuento '" + nombre + "' creado exitosamente.");
        return "redirect:/admin/descuentos";
    }

    @PostMapping("/{id}/toggle-baja")
    public String toggleBaja(@PathVariable Integer id, RedirectAttributes ra) {
        descuentoRepo.findById(id).ifPresent(d -> {
            d.setBaja(!d.getBaja());
            d.setUltimaModificacion(LocalDateTime.now());
            descuentoRepo.save(d);
        });
        ra.addFlashAttribute("mensaje", "Estado del descuento actualizado.");
        return "redirect:/admin/descuentos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarDescuento(@PathVariable Integer id, RedirectAttributes ra) {
        descuentoRepo.deleteById(id);
        ra.addFlashAttribute("mensaje", "Descuento eliminado del sistema.");
        return "redirect:/admin/descuentos";
    }
}
