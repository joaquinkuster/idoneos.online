package com.app.idoneos.controller.modulo_auditoria;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * TRAZABILIDAD — Controller para el Módulo de Auditoría (MOD-NF-08).
 *
 * Mapea la pantalla de Auditoría:
 *   CU-95 — Consultar auditoría → GET /auditoria
 */
@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    @Autowired private AuditoriaRepository auditoriaRepository;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    /**
     * CU-95 — Consultar auditoría.
     * Vista: cu-95-consultar-auditoria.html
     */
    @GetMapping
    public String consultarAuditoria(@RequestParam(required = false) String entidad,
                                     Model model,
                                     Authentication auth) {
        agregarUsuarioAlModelo(model, auth);

        List<Auditoria> registros;
        if (entidad != null && !entidad.isBlank()) {
            registros = auditoriaRepository.findByEntidadAfectadaContainingIgnoreCaseOrderByFechaHoraDesc(entidad.trim());
        } else {
            registros = auditoriaRepository.findAllByOrderByFechaHoraDesc();
        }

        model.addAttribute("registros", registros);
        model.addAttribute("filtroEntidad", entidad != null ? entidad : "");
        model.addAttribute("titulo", "CU-95 - Consultar auditoría | Idóneos Online");
        return "pages/auditoria/cu-95-consultar-auditoria";
    }
}
