package com.app.idoneos.controller.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
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
 * TRAZABILIDAD — Controller para la gestión de descuentos y cupones
 * promocionales.
 *
 * MOD-F-03: Módulo de Inscripciones y Pagos
 * CU-49 — Buscar descuento → GET /admin/descuentos
 * Lista todos los descuentos (activos e inactivos).
 * CU-50 — Registrar descuento → POST /admin/descuentos/crear
 * Actor: Administrador. Postcondición: descuento registrado con % y vigencia.
 * CU-51 — Modificar descuento → GET/POST /admin/descuentos/{id}/editar
 * Actor: Administrador.
 * CU-52 — Dar de baja descuento → POST /admin/descuentos/{id}/eliminar
 * POST /admin/descuentos/{id}/toggle-baja (toggle)
 * Actor: Administrador.
 *
 * NOTAS DE COBERTURA:
 * CU-49 paso 4-5: lista todos los descuentos. No implementa filtros por
 * vigencia ni nombre.
 * CU-50 paso 4: valida porcentaje (1-100%) y coherencia de fechas (EX-CU50-01,
 * EX-CU50-02).
 * CU-51 paso 4-5: valida mismas reglas de negocio que CU-50.
 * CU-52: implementa tanto la baja definitiva (/eliminar) como el toggle
 * activo/inactivo (/toggle-baja).
 *
 * Aplica reglas de negocio:
 * RN-CU50-01: El porcentaje debe estar entre 1% y 100%.
 * RN-CU50-02: La fecha de fin de vigencia debe ser posterior a la inicial.
 */
@Controller
@RequestMapping("/admin/descuentos")
public class DescuentoController {

    @Autowired
    private DescuentoRepository descuentoRepo;

    /**
     * TRAZABILIDAD: CU-49 — Buscar descuento.
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador. Existe al menos un descuento.
     * Flujo paso 4-5: recupera y lista todos los descuentos del sistema.
     * NOTA PARCIAL: CU-49 especifica filtros por nombre y vigencia. No
     * implementados.
     */
    @GetMapping
    public String listarDescuentos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("descuentos", descuentoRepo.findAll());
        model.addAttribute("titulo", "Gestión de Descuentos | Idóneos Online");
        return "pages/admin/descuentos";
    }

    /**
     * TRAZABILIDAD: CU-50 — Registrar descuento.
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador.
     * Flujo paso 3-5: valida porcentaje y coherencia de fechas; registra el
     * descuento.
     * Postcondición: descuento registrado en estado activo.
     * EX-CU50-01 (paso 4): porcentaje fuera de rango [1, 100] → redirect con
     * mensaje.
     * EX-CU50-02 (paso 5): fecha de fin anterior a fecha de inicio → redirect con
     * mensaje.
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
            ra.addFlashAttribute("mensaje", "EX-CU50-01: El porcentaje debe estar entre 1% y 100%.");
            return "redirect:/admin/descuentos";
        }
        if (vigenciaHasta.isBefore(vigenciaDesde)) {
            ra.addFlashAttribute("mensaje", "EX-CU50-02: La fecha de fin de vigencia debe ser posterior a la inicial.");
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

    /**
     * TRAZABILIDAD: CU-51 — Modificar descuento (formulario GET).
     * Actor: Administrador.
     * Precondición: el descuento existe.
     * Flujo paso 2: muestra los datos actuales del descuento para su edición.
     */
    @GetMapping("/{id}/editar")
    public String editarDescuentoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Descuento> dOpt = descuentoRepo.findById(id);
        if (dOpt.isEmpty())
            return "redirect:/admin/descuentos";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("descuentoEditar", dOpt.get());
        model.addAttribute("titulo", "Editar Descuento | Idóneos Online");
        return "pages/admin/editar-descuento";
    }

    /**
     * TRAZABILIDAD: CU-51 — Modificar descuento (guardar cambios).
     * Actor: Administrador.
     * Flujo paso 4-6: valida porcentaje y coherencia de fechas; guarda los cambios.
     * Postcondición: descuento actualizado.
     * EX-CU51-01 (paso 4): porcentaje fuera de rango → redirect con mensaje.
     * EX-CU51-02 (paso 5): fecha de fin anterior a inicio → redirect con mensaje.
     */
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
        if (dOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Descuento no encontrado.");
            return "redirect:/admin/descuentos";
        }
        if (porcentaje < 1 || porcentaje > 100) {
            ra.addFlashAttribute("mensaje", "EX-CU51-01: El porcentaje debe estar entre 1% y 100%.");
            return "redirect:/admin/descuentos/" + id + "/editar";
        }
        if (vigenciaHasta.isBefore(vigenciaDesde)) {
            ra.addFlashAttribute("mensaje", "EX-CU51-02: La fecha de fin debe ser posterior a la de inicio.");
            return "redirect:/admin/descuentos/" + id + "/editar";
        }

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

    /**
     * TRAZABILIDAD: CU-52 — Dar de baja descuento (Toggle activo/inactivo).
     * Actor: Administrador.
     * Flujo paso 4: alterna el estado activo/inactivo del descuento.
     * Postcondición: estado del descuento actualizado.
     */
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

    /**
     * TRAZABILIDAD: CU-52 — Dar de baja descuento (Baja lógica definitiva).
     * Actor: Administrador.
     * Flujo paso 4: establece baja = true en el descuento.
     * Postcondición: descuento dado de baja. No se elimina físicamente.
     * NOTA: el descuento dado de baja ya no puede aplicarse en nuevas inscripciones
     * (PagoService).
     */
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
