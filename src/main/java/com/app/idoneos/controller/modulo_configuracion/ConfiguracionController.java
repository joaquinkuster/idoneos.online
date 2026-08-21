package com.app.idoneos.controller.modulo_configuracion;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Configuracion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.repository.modulo_configuracion.ConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * TRAZABILIDAD — Controller para la gestión de parámetros operativos del sistema.
 *
 * MOD-NF-04: Módulo de Configuración del Sistema
 *   CU-99 — Configurar parámetros del sistema → GET  /admin/configuracion
 *                                                 POST /admin/configuracion/guardar
 *                                                 POST /admin/configuracion/borrar/{id}
 *             Actor: Administrador.
 *             Permite listar, registrar/modificar y eliminar parámetros clave-valor del sistema.
 *
 * NOTAS DE COBERTURA:
 *   CU-99 paso 2-3: listar todos los parámetros de configuración del sistema.
 *   CU-99 paso 4 (registrar): si la clave ya existe → se actualiza el valor (upsert).
 *     Si la clave no existe → se crea un nuevo parámetro.
 *   CU-99 paso 4 (EX-CU99-01): clave o valor vacíos → ExcepcionValidacion lanzada.
 *   CU-99 paso 5 (eliminar): el parámetro se elimina físicamente de la BD.
 *     NOTA: la eliminación física puede afectar integraciones externas (ej: heygen.api_key,
 *     mercadopago.access_token). Se recomienda baja lógica en producción.
 *
 * Parámetros clave del sistema:
 *   - "heygen.api_key"             → API Key de HeyGen (CU-74 Generar clase Clon IA).
 *   - "mercadopago.access_token"   → Token de acceso de Mercado Pago (CU-47 Realizar pago).
 *   - "ollama.base_url"            → URL del servidor Ollama local (CU-70, CU-71, CU-72).
 *   - "smtp.from"                  → Dirección de correo remitente (CU-36, CU-40, CU-47 notificaciones).
 *
 * El acceso a /admin/configuracion está restringido a Administrador por SecurityConfig.
 */
@Controller
@RequestMapping("/admin/configuracion")
public class ConfiguracionController {

    @Autowired private ConfiguracionRepository configRepo;

    /**
     * TRAZABILIDAD: CU-99 — Configurar parámetros del sistema (listar).
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador.
     * Flujo paso 2-3: recupera y lista todos los parámetros de configuración del sistema.
     */
    @GetMapping
    public String verConfiguracion(Model model, Authentication auth) {
        // CU-99 paso 2-3: lista todos los parámetros de configuración.
        List<Configuracion> parametros = configRepo.findAll();
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("parametros", parametros);
        model.addAttribute("titulo", "Configuración de Parámetros | Idóneos Online");
        return "pages/admin/configuracion";
    }

    /**
     * TRAZABILIDAD: CU-99 — Configurar parámetros del sistema (registrar o modificar).
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador. Clave y valor son campos obligatorios.
     * Flujo paso 4: si la clave ya existe → se actualiza el valor (upsert).
     *               Si la clave no existe → se registra el nuevo parámetro.
     * Postcondición: parámetro registrado o actualizado.
     * EX-CU99-01 (paso 4): clave o valor vacíos → ExcepcionValidacion (HTTP 400).
     */
    @PostMapping("/guardar")
    public String guardarParametro(@RequestParam String clave,
                                   @RequestParam String valor,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        // CU-99 EX-CU99-01: validar que clave y valor sean obligatorios.
        if (clave == null || clave.trim().isEmpty() || valor == null || valor.trim().isEmpty()) {
            throw new ExcepcionValidacion("EX-CU99-01: La clave y el valor del parámetro son obligatorios.");
        }

        Usuario usuarioActual = (Usuario) auth.getPrincipal();
        Administrador adminActual = usuarioActual != null ? usuarioActual.getAdministrador() : null;

        // CU-99 paso 4: upsert del parámetro (busca por clave; si no existe lo crea).
        Configuracion c = configRepo.findByClave(clave.trim())
                .orElseGet(() -> new Configuracion(clave.trim(), valor.trim(), adminActual));
        c.setValor(valor.trim());
        if (c.getAdministrador() == null && adminActual != null) {
            c.setAdministrador(adminActual);
        }
        configRepo.save(c);
        ra.addFlashAttribute("mensaje", "Parámetro '" + clave + "' guardado correctamente.");
        return "redirect:/admin/configuracion";
    }

    /**
     * TRAZABILIDAD: CU-99 — Configurar parámetros del sistema (eliminar).
     * Actor: Administrador.
     * Precondición: el parámetro existe.
     * Flujo paso 5: elimina físicamente el parámetro de la tabla Configuracion.
     * Postcondición: parámetro eliminado del sistema.
     * NOTA: la eliminación es física. En producción considerar baja lógica para preservar
     *   la trazabilidad de configuraciones pasadas.
     */
    @PostMapping("/borrar/{id}")
    public String borrarParametro(@PathVariable Integer id, RedirectAttributes ra) {
        // CU-99 paso 5: eliminar el parámetro por ID.
        configRepo.deleteById(id);
        ra.addFlashAttribute("mensaje", "Parámetro eliminado.");
        return "redirect:/admin/configuracion";
    }
}

