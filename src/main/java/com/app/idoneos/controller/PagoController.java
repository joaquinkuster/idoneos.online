package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.service.Curso.CursoServiceImpl;
import com.app.idoneos.service.Inscripcion.InscripcionServiceImpl;
import com.app.idoneos.service.Pago.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para la pasarela de pagos (CU-44: Buscar pago / CU-45: Realizar pago).
 * Integra la Checkout API de Mercado Pago, cálculo de cupones promocionales y emisión del comprobante.
 */
@Controller
@RequestMapping("/pago")
public class PagoController {

    @Autowired private PagoService pagoService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private InscripcionServiceImpl inscripcionService;

    /**
     * CU-45 — Pantalla de Checkout y resumen de pago con aplicación de cupones de descuento.
     */
    @GetMapping("/checkout/{cursoId}")
    public String verCheckout(@PathVariable Integer cursoId, Model model, Authentication auth) {
        Curso curso = cursoService.buscarPorId(cursoId).orElse(null);
        if (curso == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        Double montoFinal = pagoService.calcularMontoConDescuento(usuario, curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("montoOriginal", curso.getPrecio());
        model.addAttribute("montoFinal", montoFinal);
        model.addAttribute("tieneDescuento", montoFinal < curso.getPrecio());
        model.addAttribute("titulo", "Checkout | " + curso.getNombre());
        return "pages/alumno/checkout";
    }

    /**
     * CU-45 — Procesar pago con tarjeta / Mercado Pago API y activar la inscripción.
     * Reglas de negocio:
     * - Invocación a la API de Mercado Pago (`/v1/payments`).
     * - Generación de estado 'Acreditado' y comprobante automático en PDF.
     * - Envío de correo electrónico de confirmación al alumno.
     */
    @PostMapping("/procesar/{cursoId}")
    public String procesarPago(@PathVariable Integer cursoId,
                               @RequestParam String nombreTarjeta,
                               @RequestParam String numeroTarjeta,
                               Authentication auth,
                               RedirectAttributes ra) {
        Curso curso = cursoService.buscarPorId(cursoId).orElse(null);
        if (curso == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();

        Inscripcion inscripcion = inscripcionService.inscribirAlumno(usuario, curso);

        Double monto = pagoService.calcularMontoConDescuento(usuario, curso);
        String ultimos4 = numeroTarjeta.length() >= 4 ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : "1234";

        Pago pago = pagoService.procesarPagoTarjeta(inscripcion, monto, usuario.getCorreo(), nombreTarjeta, ultimos4);

        ra.addFlashAttribute("pago", pago);
        ra.addFlashAttribute("mensaje", "¡Pago acreditado correctamente! Tu inscripción ya está activa.");
        return "redirect:/pago/resultado/" + pago.getId();
    }

    /**
     * CU-44 — Ver resultado del pago y descargar comprobante oficial emitido.
     */
    @GetMapping("/resultado/{pagoId}")
    public String resultadoPago(@PathVariable Integer pagoId, Model model, Authentication auth) {
        Pago pago = pagoService.buscarPorId(pagoId).orElse(null);
        if (pago == null) return "redirect:/cursos";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("pago", pago);
        model.addAttribute("titulo", "Comprobante de Pago | Idóneos Online");
        return "pages/alumno/pago-resultado";
    }
}
