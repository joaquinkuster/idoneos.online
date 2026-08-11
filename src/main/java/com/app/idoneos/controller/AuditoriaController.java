package com.app.idoneos.controller;

import com.app.idoneos.model.Auditoria;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller para la consulta del Log de Auditoría por parte del Administrador (CU-89: Consultar log de auditoría).
 * Muestra el registro de acciones realizadas en el sistema ordenadas cronológicamente con opción de filtrado por entidad.
 */
@Controller
@RequestMapping("/admin/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    /**
     * CU-89 — Consultar log de auditoría.
     * Permite al Administrador filtrar por entidad afectada (ej. Curso, Usuario, Pago) y visualizar los eventos.
     */
    @GetMapping
    public String verAuditoria(@RequestParam(required = false) String entidad,
                               Model model,
                               Authentication auth) {
        List<Auditoria> registros;
        if (entidad != null && !entidad.isBlank()) {
            registros = auditoriaRepository.findByEntidadAfectadaContainingIgnoreCaseOrderByFechaHoraDesc(entidad.trim());
        } else {
            registros = auditoriaRepository.findAllByOrderByFechaHoraDesc();
        }

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("registros", registros);
        model.addAttribute("filtroEntidad", entidad != null ? entidad : "");
        model.addAttribute("titulo", "Log de Auditoría | Idóneos Online");
        return "pages/admin/auditoria";
    }
}
