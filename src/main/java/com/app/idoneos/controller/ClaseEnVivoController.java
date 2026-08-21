package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * TRAZABILIDAD — Controller para la programación y transmisión de Clases en Vivo.
 *
 * MOD-F-05: Módulo de Clases en Vivo
 *   CU-65 — Buscar clase en vivo       → GET /clase-vivo/docente
 *             Actor: Docente o Administrador. Lista las clases del docente activas (baja = false) por fecha desc.
 *             NOTA PARCIAL: CU-65 especifica filtros por unidad, título, docente, rango de fechas y estado. No implementados.
 *   CU-66 — Programar clase en vivo    → POST /clase-vivo/programar
 *             Actor: Docente. Establece fecha/hora, título y unidad asociada.
 *             NOTA PARCIAL: CU-66 pasos 6-7 validan que no haya superposición con otra clase del mismo docente.
 *               Y que la fecha esté dentro de las fechas de dictado de la cohorte. No implementados. PARCIAL.
 *   CU-67 — Modificar clase en vivo    → POST /clase-vivo/{claseId}/modificar
 *             Actor: Docente. Solo clases en estado "Programada".
 *             NOTA PARCIAL: CU-67 paso 2 verifica que la clase esté en estado "Programada". No implementado. PARCIAL.
 *   CU-68 — Cancelar clase en vivo     → POST /clase-vivo/{claseId}/cancelar
 *             Actor: Docente. Baja lógica (baja = true).
 *             NOTA PARCIAL: CU-68 paso 4 notifica a los alumnos. No implementado. PARCIAL.
 *   CU-69 — Dar de baja clase en vivo  → no implementado como endpoint separado para Admin. FALTANTE.
 *             Actor: Administrador. Solo clases en estado "Finalizada".
 *   CU-70 — Iniciar clase en vivo      → POST /clase-vivo/{claseId}/iniciar
 *             Actor: Docente. Genera URL RTMP y Stream Key. Cambia estado a "En vivo".
 *   CU-71 — Finalizar clase en vivo    → POST /clase-vivo/{claseId}/finalizar
 *             Actor: Docente. Cambia estado a "Finalizada". Genera material de grabación en estado no publicado.
 *             NOTA PARCIAL: CU-71 paso 6 notifica a los alumnos. No implementado. PARCIAL.
 *   CU-72 — Ingresar a clase en vivo   → GET /clase-vivo/{claseId}/ver
 *             Actor: Alumno con inscripción vigente al curso.
 *             NOTA PARCIAL: CU-72 paso 2 verifica inscripción vigente. No implementado en este controller. FALTANTE.
 *
 * NOTAS DE COBERTURA:
 *   CU-66 EX-CU66-01 (paso 4): si la unidad no existe o el docente no tiene sesión → redirect con mensaje.
 *   CU-66 paso 8: el estado "Programada" debe existir en la tabla EstadoClaseEnVivo.
 *   CU-70: la URL RTMP se genera como "rtmp://live.idoneos.online/stream/{claseId}" y la Stream Key
 *     como un UUID de 16 caracteres. Integración real con servidor de streaming pendiente.
 *   CU-71 paso 5: el material de grabación se crea con publicado = false (requiere revisión del docente).
 */
@Controller
@RequestMapping("/clase-vivo")
public class ClaseEnVivoController {

    @Autowired private ClaseEnVivoRepository claseEnVivoRepository;
    @Autowired private EstadoClaseEnVivoRepository estadoRepo;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private CronogramaRepository cronogramaRepository;

    /**
     * TRAZABILIDAD: CU-65 — Buscar clase en vivo.
     * Actor: Docente (o Administrador).
     * Precondición: sesión con rol Docente. Existe al menos una clase programada o activa.
     * Flujo paso 4: recupera y lista las clases en vivo del docente (baja = false) por fecha desc.
     * NOTA PARCIAL: CU-65 especifica filtros por unidad, título, docente, rango de fechas y estado. No implementados.
     */
    @GetMapping("/docente")
    public String misClases(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        if (docente == null) return "redirect:/docente";

        // CU-65 paso 4: lista clases activas del docente ordenadas por fecha descendente.
        List<ClaseEnVivo> clases = claseEnVivoRepository.findByDocenteAndBajaFalseOrderByFechaHoraDesc(docente);
        model.addAttribute("usuario", usuario);
        model.addAttribute("clases", clases);
        model.addAttribute("titulo", "Mis Clases en Vivo | Idóneos Online");
        return "pages/docente/clases-en-vivo";
    }

    /**
     * TRAZABILIDAD: CU-66 — Programar clase en vivo.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. El docente existe. La unidad existe.
     *   El curso posee al menos una cohorte con fechas de dictado. Estado "Programada" configurado.
     * Flujo paso 3-4: valida unidad, docente y estado. Registra la clase con fecha/hora futura.
     * Postcondición: clase en vivo registrada en estado "Programada".
     * EX-CU66-01 (paso 4): datos incompletos (docente, unidad, estado ausente) → redirect con mensaje.
     * NOTA PARCIAL: CU-66 pasos 5-7 validan que la fecha sea posterior al momento actual, que esté dentro
     *   de las fechas de dictado de la cohorte, y que no haya superposición con otra clase. No implementados.
     * NOTA PARCIAL: CU-66 paso 9 notifica a los alumnos. No implementado.
     */
    @PostMapping("/programar")
    public String programar(@RequestParam Integer unidadId,
                            @RequestParam String titulo,
                            @RequestParam String fechaHora,
                            Authentication auth,
                            RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        EstadoClaseEnVivo estadoProgramada = estadoRepo.findByNombre("Programada").orElse(null);

        // De acuerdo al modelo relacional: ClaseEnVivo -> Cohorte -> Programa -> Cronograma -> Unidad
        Cohorte cohorte = null;
        if (unidad != null) {
            List<Cronograma> cronogramas = cronogramaRepository.findByUnidad(unidad);
            for (Cronograma crono : cronogramas) {
                if (crono.getPrograma() != null) {
                    List<Cohorte> cohortes = cohorteRepository.findByProgramaAndBajaFalse(crono.getPrograma());
                    if (!cohortes.isEmpty()) {
                        cohorte = cohortes.get(0);
                        break;
                    }
                }
            }
        }

        if (docente == null || unidad == null || estadoProgramada == null || cohorte == null) {
            ra.addFlashAttribute("mensaje", "EX-CU66-01: Datos incompletos o cohorte no encontrada al programar la clase.");
            return "redirect:/clase-vivo/docente";
        }

        // CU-66 paso 8: registra la clase en vivo con estado "Programada".
        LocalDateTime dt = LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        ClaseEnVivo clase = new ClaseEnVivo(titulo, dt, docente, estadoProgramada, cohorte);
        claseEnVivoRepository.save(clase);
        ra.addFlashAttribute("mensaje", "Clase programada para " + dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-67 — Modificar clase en vivo.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La clase existe y está en estado "Programada".
     * Flujo paso 3-4: actualiza el título y/o la fecha/hora de la clase.
     * Postcondición: clase en vivo modificada. Alumnos notificados si cambió la fecha.
     * NOTA PARCIAL: CU-67 paso 2 verifica que la clase esté en estado "Programada". No implementado. PARCIAL.
     * NOTA PARCIAL: CU-67 pasos 5-6 validan fecha dentro del período de dictado y no superposición. FALTANTE.
     * NOTA PARCIAL: CU-67 paso 8 notifica a los alumnos el cambio. No implementado. FALTANTE.
     */
    @PostMapping("/{claseId}/modificar")
    public String modificar(@PathVariable Integer claseId,
                            @RequestParam String titulo,
                            @RequestParam String fechaHora,
                            RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase == null) return "redirect:/clase-vivo/docente";

        LocalDateTime dt = LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        clase.setTitulo(titulo);
        clase.setFechaHora(dt);
        claseEnVivoRepository.save(clase);

        ra.addFlashAttribute("mensaje", "Clase modificada correctamente.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-68 — Cancelar clase en vivo.
     * Actor: Docente.
     * Precondición: la clase existe y está en estado "Programada".
     * Flujo paso 3: aplica baja lógica (baja = true) a la clase en vivo.
     * Postcondición: clase cancelada (dada de baja).
     * NOTA PARCIAL: CU-68 paso 4 requiere notificación a los alumnos inscriptos. No implementado. FALTANTE.
     */
    @PostMapping("/{claseId}/cancelar")
    public String cancelar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase != null) {
            clase.setBaja(true);
            claseEnVivoRepository.save(clase);
        }
        ra.addFlashAttribute("mensaje", "Clase en vivo cancelada.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-70 — Iniciar clase en vivo.
     * Actor: Docente.
     * Precondición: la clase existe en estado "Programada". Se alcanzó el horario programado.
     *   Estado "En vivo" configurado en la BD.
     * Flujo paso 2-3: genera los datos de conexión (URL RTMP y Stream Key). Cambia estado a "En vivo".
     * Postcondición: clase en estado "En vivo". Datos de conexión generados para OBS.
     * NOTA PARCIAL: CU-70 paso 5 redistribuye la señal a los alumnos e inicia grabación automática.
     *   Integración real con servidor de streaming (RTMP) pendiente. URL y clave son sintéticas.
     */
    @PostMapping("/{claseId}/iniciar")
    public String iniciar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        EstadoClaseEnVivo estadoEnVivo = estadoRepo.findByNombre("En vivo").orElse(null);

        if (clase == null || estadoEnVivo == null) {
            ra.addFlashAttribute("mensaje", "No se pudo iniciar la clase.");
            return "redirect:/clase-vivo/docente";
        }

        // CU-67 paso 4-5: genera URL RTMP y Stream Key, actualiza estado.
        String urlRtmp = "rtmp://live.idoneos.online/stream/" + claseId;
        String claveStream = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        clase.setEstado(estadoEnVivo);
        clase.setUrlRtmp(urlRtmp);
        clase.setClaveStream(claveStream);
        claseEnVivoRepository.save(clase);

        ra.addFlashAttribute("mensaje", "Clase iniciada. URL RTMP: " + urlRtmp + " | Clave: " + claveStream);
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-71 — Finalizar clase en vivo.
     * Actor: Docente.
     * Precondición: la clase existe en estado "En vivo". Estado "Finalizada" configurado.
     * Flujo paso 2: envía la orden de corte de transmisión y grabación al OBS del docente.
     * Flujo paso 3: cambia el estado a "Finalizada".
     * Flujo paso 4-5: genera el material de grabación con publicado = false (pendiente de revisión).
     * Postcondición: clase finalizada. Material de grabación cargado como tipo Grabación sin publicar.
     * NOTA PARCIAL: CU-71 paso 2 (corte remoto de OBS) no implementado. FALTANTE.
     * NOTA PARCIAL: CU-71 paso 6 notifica a los alumnos la disponibilidad de la grabación. FALTANTE.
     */
    @PostMapping("/{claseId}/finalizar")
    public String finalizar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        EstadoClaseEnVivo estadoFinalizada = estadoRepo.findByNombre("Finalizada").orElse(null);
        TipoMaterial tipoGrabacion = tipoMaterialRepository.findByNombre("Grabación").orElse(null);

        if (clase == null || estadoFinalizada == null) {
            ra.addFlashAttribute("mensaje", "No se pudo finalizar la clase.");
            return "redirect:/clase-vivo/docente";
        }

        // CU-68 paso 4: cambia estado a "Finalizada".
        clase.setEstado(estadoFinalizada);

        // CU-68 paso 5: genera el material de grabación en estado "En revisión" (publicado = false).
        if (tipoGrabacion != null) {
            String rutaGrabacion = "grabaciones/clase_" + claseId + "_" + System.currentTimeMillis() + ".mp4";
            String tituloMat = "Grabación: " + clase.getTitulo();
            if (tituloMat.length() > 50) tituloMat = tituloMat.substring(0, 47) + "...";

            // Obtener unidad a través del cronograma del programa de la cohorte si está disponible
            Unidad unidadMaterial = null;
            if (clase.getCohorte() != null && clase.getCohorte().getPrograma() != null) {
                List<Cronograma> cronos = cronogramaRepository.findByProgramaOrderByNumeroOrden(clase.getCohorte().getPrograma());
                if (!cronos.isEmpty()) {
                    unidadMaterial = cronos.get(0).getUnidad();
                }
            }

            if (unidadMaterial != null) {
                Material grabacion = new Material(tituloMat, clase.getDocente(), tipoGrabacion, unidadMaterial);
                grabacion.setRutaArchivo(rutaGrabacion);
                grabacion.setPublicado(false);
                materialRepository.save(grabacion);
                clase.setMaterial(grabacion);
            }
        }

        claseEnVivoRepository.save(clase);
        ra.addFlashAttribute("mensaje", "Clase finalizada. La grabación está disponible para revisión.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-72 — Ingresar a clase en vivo.
     * Actor: Alumno con inscripción vigente al curso de la clase.
     * Precondición: sesión con rol Alumno. La clase existe en estado "En vivo".
     * Flujo paso 2-3: verifica que la clase esté en estado "En vivo" y conecta al alumno a la transmisión.
     * EX-CU72-01 (paso 2): si la clase no está "En vivo" → la vista muestra mensaje de no disponible.
     * NOTA PARCIAL: CU-72 paso 2 verifica que el alumno tenga inscripción vigente al curso.
     *   La verificación de inscripción no está implementada en este controller. FALTANTE.
     */
    @GetMapping("/{claseId}/ver")
    public String verClase(@PathVariable Integer claseId, Model model, Authentication auth) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase == null) return "redirect:/cursos";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("clase", clase);
        model.addAttribute("titulo", "Clase en Vivo: " + clase.getTitulo());
        return "pages/alumno/ver-clase-vivo";
    }
}
