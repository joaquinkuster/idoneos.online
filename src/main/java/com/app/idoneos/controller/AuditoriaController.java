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
 * TRAZABILIDAD — Controller para la consulta del Log de Auditoría del sistema.
 *
 * MOD-NF-02: Módulo de Auditoría
 *   CU-95 — Consultar auditoría → GET /admin/auditoria
 *             Actor: Administrador.
 *             Precondición: sesión con rol Administrador. Existen registros de auditoría.
 *             Flujo paso 2: muestra el historial completo de acciones registradas en el sistema.
 *             Flujo paso 3: permite filtrar por entidad afectada (ej: "Curso", "Usuario", "Pago").
 *             Flujo paso 4-5: recupera y lista los registros ordenados cronológicamente descendente.
 *
 * NOTAS DE COBERTURA:
 *   CU-95 paso 3: el filtro por entidad afectada se hace con LIKE (contiene) sobre el campo
 *     entidadAfectada. El filtro es case-insensitive.
 *   CU-95 paso 4-5: los registros se ordenan por fechaHora de forma descendente.
 *   CU-95 paso 3 (filtros adicionales): CU-95 especifica también filtro por acción (tipo de acción),
 *     rango de fechas y usuario responsable. No implementados. IMPLEMENTADO PARCIALMENTE.
 *   CU-95: la generación de registros de auditoría se realiza mediante AuditoriaInterceptor
 *     (o AuditoriaAspect) que persiste automáticamente cada acción relevante.
 *
 * El acceso al endpoint /admin/auditoria está restringido a Administrador por SecurityConfig.
 */
@Controller
@RequestMapping("/admin/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    /**
     * TRAZABILIDAD: CU-95 — Consultar auditoría.
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador. Existen registros en la tabla Auditoria.
     * Flujo paso 2: muestra el formulario de filtro y el historial de eventos de auditoría.
     * Flujo paso 3: si se proporciona el parámetro "entidad", filtra por entidad afectada (LIKE, case-insensitive).
     *   Si no se proporciona, lista todos los registros.
     * Flujo paso 4-5: recupera los registros ordenados por fechaHora descendente.
     * Postcondición: lista de eventos de auditoría visible para el Administrador.
     * NOTA PARCIAL: CU-95 especifica filtros adicionales (acción, rango de fechas, usuario responsable).
     *   Solo está implementado el filtro por entidad afectada. IMPLEMENTADO PARCIALMENTE.
     */
    @GetMapping
    public String verAuditoria(@RequestParam(required = false) String entidad,
                               Model model,
                               Authentication auth) {
        List<Auditoria> registros;
        if (entidad != null && !entidad.isBlank()) {
            // CU-95 paso 3: filtro por entidad afectada (LIKE, case-insensitive).
            registros = auditoriaRepository.findByEntidadAfectadaContainingIgnoreCaseOrderByFechaHoraDesc(entidad.trim());
        } else {
            // CU-95 paso 4: lista todos los registros ordenados por fecha descendente.
            registros = auditoriaRepository.findAllByOrderByFechaHoraDesc();
        }

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("registros", registros);
        model.addAttribute("filtroEntidad", entidad != null ? entidad : "");
        model.addAttribute("titulo", "Log de Auditoría | Idóneos Online");
        return "pages/admin/auditoria";
    }
}
