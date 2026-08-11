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
import java.util.Optional;

/**
 * Controller para la gestión de descuentos y cupones promocionales (CU-47 a CU-50).
 */
@Controller
@RequestMapping("/admin/descuentos")
public class DescuentoController {

    @Autowired
    private DescuentoRepository descuentoRepo;

    /** CU-47 — Consultar / listar descuentos. */
    @GetMapping
    public String listarDescuentos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("descuentos", descuentoRepo.findAll());
        model.addAttribute("titulo", "Gestión de Descuentos | Idóneos Online");
        return "pages/admin/descuentos";
    }

    /**
     * CU-48 — Registrar nuevo descuento / cupón promocional.
     * Reglas de negocio:
     * - Porcentaje entre 1% y 100% (Excepción CU-48, paso 4).
     * - Coherencia de rango de vigencia (Excepción CU-48, paso 5).
     */
    @PostMapping("/crear")
    public String crearDescuento(@RequestParam String nombre,
                                 @RequestParam Double porcentaje,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime vigenciaDesde,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime vigenciaHasta,
                                 @RequestParam(required = false) Integer cantidadLimite,
                                 @RequestParam(defaultValue = "0") Integer cursosRequeridos,
                                 RedirectAttributes ra) {

        if (porcentaje < 1 || porcentaje > 100) {
            ra.addFlashAttribute("mensaje", "CU-48 Excepción paso 4: El porcentaje debe estar entre 1% y 100%.");
            return "redirect:/admin/descuentos";
        }
        if (vigenciaHasta.isBefore(vigenciaDesde)) {
            ra.addFlashAttribute("mensaje", "CU-48 Excepción paso 5: La fecha de fin de vigencia debe ser posterior a la inicial.");
            return "redirect:/admin/descuentos";
        }

        Descuento d = new Descuento();
        d.setNombre(nombre);
        d.setPorcentaje(porcentaje.floatValue());
        d.setVigenciaDesde(vigenciaDesde);
        d.setVigenciaHasta(vigenciaHasta);
        d.setCantidadLimite(cantidadLimite != null ? cantidadLimite : 0);
        d.setCursosRequeridos(cursosRequeridos);
        d.setFechaCreacion(LocalDateTime.now());

        descuentoRepo.save(d);
        ra.addFlashAttribute("mensaje", "Descuento '" + nombre + "' creado exitosamente.");
        return "redirect:/admin/descuentos";
    }

    /** CU-49 — Modificar descuento (formulario). */
    @GetMapping("/{id}/editar")
    public String editarDescuentoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Descuento> dOpt = descuentoRepo.findById(id);
        if (dOpt.isEmpty()) return "redirect:/admin/descuentos";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("descuentoEditar", dOpt.get());
        model.addAttribute("titulo", "Editar Descuento | Idóneos Online");
        return "pages/admin/editar-descuento";
    }

    /** CU-49 — Modificar descuento (guardar cambios). */
    @PostMapping("/{id}/editar")
    public String guardarEdicionDescuento(@PathVariable Integer id,
                                          @RequestParam String nombre,
                                          @RequestParam Double porcentaje,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime vigenciaDesde,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime vigenciaHasta,
                                          @RequestParam(required = false) Integer cantidadLimite,
                                          @RequestParam(defaultValue = "0") Integer cursosRequeridos,
                                          RedirectAttributes ra) {
        Optional<Descuento> dOpt = descuentoRepo.findById(id);
        if (dOpt.isEmpty()) { ra.addFlashAttribute("mensaje", "Descuento no encontrado."); return "redirect:/admin/descuentos"; }
        if (porcentaje < 1 || porcentaje > 100) { ra.addFlashAttribute("mensaje", "CU-49 Excepción paso 4: El porcentaje debe estar entre 1% y 100%."); return "redirect:/admin/descuentos/" + id + "/editar"; }
        if (vigenciaHasta.isBefore(vigenciaDesde)) { ra.addFlashAttribute("mensaje", "CU-49 Excepción paso 5: La fecha de fin debe ser posterior a la de inicio."); return "redirect:/admin/descuentos/" + id + "/editar"; }

        Descuento d = dOpt.get();
        d.setNombre(nombre);
        d.setPorcentaje(porcentaje.floatValue());
        d.setVigenciaDesde(vigenciaDesde);
        d.setVigenciaHasta(vigenciaHasta);
        d.setCantidadLimite(cantidadLimite != null ? cantidadLimite : 0);
        d.setCursosRequeridos(cursosRequeridos);
        d.setUltimaModificacion(LocalDateTime.now());
        descuentoRepo.save(d);
        ra.addFlashAttribute("mensaje", "Descuento actualizado correctamente.");
        return "redirect:/admin/descuentos";
    }

    /** CU-50 — Toggle estado de baja lógica del descuento. */
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

    /** CU-50 — Dar de baja un descuento (Baja Lógica). */
    @PostMapping("/{id}/eliminar")
    public String eliminarDescuento(@PathVariable Integer id, RedirectAttributes ra) {
        descuentoRepo.findById(id).ifPresent(d -> {
            d.setBaja(true);
            d.setUltimaModificacion(LocalDateTime.now());
            descuentoRepo.save(d);
        });
        ra.addFlashAttribute("mensaje", "Descuento dado de baja del sistema.");
        return "redirect:/admin/descuentos";
    }
}
