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
 * TRAZABILIDAD — Controller para la pasarela de pagos del sistema.
 *
 * MOD-F-03: Módulo de Inscripciones y Pagos
 *   CU-46 — Buscar pago              → GET /pago/resultado/{pagoId}
 *             Muestra el comprobante del pago y su estado.
 *   CU-47 — Realizar pago            → GET /pago/checkout/{cursoId} (formulario)
 *                                       POST /pago/procesar/{cursoId} (ejecución)
 *             Flujo: calcular monto con descuento → capturar datos de tarjeta →
 *             invocar Mercado Pago API → registrar pago → activar inscripción → emitir comprobante.
 *             Incluye la lógica de descuentos y cupones (CU-49 — Aplicar descuento).
 *
 * NOTAS DE COBERTURA:
 *   CU-47 paso 3: la vista de checkout muestra el monto original y el monto con descuento (si aplica).
 *   CU-47 paso 5: el procesamiento del pago invoca PagoService.procesarPagoTarjeta() que
 *     simula la integración con la API de Mercado Pago.
 *   CU-47 paso 6: si el pago es aprobado, se activa la inscripción y se emite un comprobante.
 *   CU-47 paso 7: si el pago es rechazado, se muestra error. No implementado el caso de rechazo explícito.
 *   CU-46 paso 4: el comprobante se descarga desde la vista del resultado (no implementado como PDF aquí).
 *   CU-45 — Dar de baja inscripción: no implementado en este controller. FALTANTE.
 *
 * Aplica reglas de negocio: RN-02 (Pago previo requerido para cursos arancelados).
 */
@Controller
@RequestMapping("/pago")
public class PagoController {

    @Autowired private PagoService pagoService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private InscripcionServiceImpl inscripcionService;

    /**
     * TRAZABILIDAD: CU-47 — Realizar pago (pantalla de Checkout).
     * TRAZABILIDAD: CU-49 — Aplicar descuento (muestra el monto con descuento calculado).
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. El alumno tiene una inscripción en estado "Pendiente de Pago"
     *   o accede al checkout directamente desde el catálogo.
     * Flujo paso 2-3: muestra el resumen del curso con monto original y monto final con descuento.
     * Flujo paso 4: presenta el formulario de datos de tarjeta para pago con Mercado Pago.
     * NOTA PARCIAL: CU-47 paso 3 también verifica cupo y ventana de inscripción. No verificado aquí.
     */
    @GetMapping("/checkout/{cursoId}")
    public String verCheckout(@PathVariable Integer cursoId, Model model, Authentication auth) {
        Curso curso = cursoService.buscarPorId(cursoId).orElse(null);
        if (curso == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        // CU-49: calcular monto con descuento vigente para el alumno.
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
     * TRAZABILIDAD: CU-47 — Realizar pago (procesamiento del pago y activación de inscripción).
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. Datos de tarjeta cargados correctamente.
     * Flujo paso 5: invoca PagoService.procesarPagoTarjeta() que simula la integración con Mercado Pago.
     * Flujo paso 6: si el pago es aprobado → inscripción activada + comprobante emitido.
     * Postcondición: Pago registrado con estado "Acreditado". Inscripción activa. Comprobante generado.
     * EX-CU47-01: pago rechazado por la pasarela → no implementado el manejo explícito de rechazo.
     * NOTA PARCIAL: CU-47 paso 8 requiere envío de correo de confirmación.
     *   El envío de correo se realiza dentro de PagoService.procesarPagoTarjeta().
     * Aplica regla RN-02: los cursos arancelados requieren pago previo para activar la inscripción.
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

        // CU-44 paso 5: registra la inscripción (o recupera la existente si ya estaba pendiente).
        Inscripcion inscripcion = inscripcionService.inscribirAlumno(usuario, curso);

        // CU-47 paso 5: calcula monto con descuento y procesa el pago con datos de tarjeta.
        Double monto = pagoService.calcularMontoConDescuento(usuario, curso);
        String ultimos4 = numeroTarjeta.length() >= 4 ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : "1234";

        Pago pago = pagoService.procesarPagoTarjeta(inscripcion, monto, usuario.getCorreo(), nombreTarjeta, ultimos4);

        ra.addFlashAttribute("pago", pago);
        ra.addFlashAttribute("mensaje", "¡Pago acreditado correctamente! Tu inscripción ya está activa.");
        return "redirect:/pago/resultado/" + pago.getId();
    }

    /**
     * TRAZABILIDAD: CU-46 — Buscar pago (ver resultado del pago y comprobante).
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. El pago existe en el sistema.
     * Flujo paso 4-5: muestra el estado del pago y presenta el comprobante de la operación.
     * NOTA PARCIAL: CU-46 especifica la descarga del comprobante en formato PDF.
     *   La generación del PDF se hace en PagoService; la vista aquí solo muestra el resumen.
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
