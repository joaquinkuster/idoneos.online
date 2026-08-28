package com.app.idoneos.service.modulo_cursos;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TRAZABILIDAD — Implementación del Servicio de Cohortes (MOD-F-01).
 *
 * Implementa CU-11 a CU-14 según Contratos.md y Casos de Uso Reales.md.
 */
@Service
@Transactional
public class CohorteServiceImpl implements CohorteService {

    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    /**
     * CU-12 — Registrar cohorte.
     * 
     * Reglas:
     * - Programa activo.
     * - Fecha fin inscripción posterior a fecha inicio inscripción.
     * - Semanas de acceso > 0.
     * - Si incluye fechas de dictado, inicio dictado posterior a fin inscripción.
     */
    @Override
    public Cohorte registrarCohorte(Integer programaId, String fechaInicioInscripcion, String fechaFinInscripcion,
                                   String fechaInicioDictado, String fechaFinDictado, int semanasAcceso, Integer cupoMaximo) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Programa no encontrado con ID: " + programaId));

        if (programa.isBaja()) {
            throw new ExcepcionNegocio("CU-12 Excepción paso 4: El programa se encuentra dado de baja.");
        }

        LocalDateTime fIniInsc = LocalDateTime.parse(fechaInicioInscripcion.contains("T") ? fechaInicioInscripcion : fechaInicioInscripcion + "T00:00:00");
        LocalDateTime fFinInsc = LocalDateTime.parse(fechaFinInscripcion.contains("T") ? fechaFinInscripcion : fechaFinInscripcion + "T23:59:59");

        if (fFinInsc.isBefore(fIniInsc)) {
            throw new ExcepcionValidacion("CU-12 Excepción paso 7: La fecha de fin de inscripción debe ser posterior a la fecha de inicio.");
        }

        if (semanasAcceso <= 0) {
            throw new ExcepcionValidacion("CU-12 Excepción paso 10: Las semanas de acceso deben ser mayores a cero.");
        }

        Cohorte cohorte = new Cohorte(fIniInsc, fFinInsc, semanasAcceso, programa);
        cohorte.setCupoMaximo(cupoMaximo != null && cupoMaximo > 0 ? cupoMaximo : null);

        if (fechaInicioDictado != null && !fechaInicioDictado.isBlank()) {
            LocalDateTime fIniDict = LocalDateTime.parse(fechaInicioDictado.contains("T") ? fechaInicioDictado : fechaInicioDictado + "T00:00:00");
            cohorte.setFechaInicioDictado(fIniDict);
        }
        if (fechaFinDictado != null && !fechaFinDictado.isBlank()) {
            LocalDateTime fFinDict = LocalDateTime.parse(fechaFinDictado.contains("T") ? fechaFinDictado : fechaFinDictado + "T23:59:59");
            cohorte.setFechaFinDictado(fFinDict);
        }

        cohorte.setBaja(false);
        cohorte.setFechaCreacion(LocalDateTime.now());
        return cohorteRepository.save(cohorte);
    }

    /**
     * CU-13 — Modificar cohorte.
     * 
     * Regla: No permite modificar fechas críticas si ya existen inscripciones activas asociadas.
     */
    @Override
    public Cohorte modificarCohorte(Integer cohorteId, String fechaInicioInscripcion, String fechaFinInscripcion,
                                   String fechaInicioDictado, String fechaFinDictado, int semanasAcceso, Integer cupoMaximo) {
        Cohorte cohorte = cohorteRepository.findById(cohorteId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Cohorte no encontrada con ID: " + cohorteId));

        if (cohorte.getBaja()) {
            throw new ExcepcionNegocio("CU-13 Excepción paso 4: La cohorte se encuentra cancelada / dada de baja.");
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByCohorte(cohorte);
        boolean tieneInscripcionesActivas = inscripciones.stream().anyMatch(i -> !i.getBaja());

        if (tieneInscripcionesActivas) {
            throw new ExcepcionConflicto("CU-13 Excepción paso 4: No se pueden modificar los datos de la cohorte porque ya posee alumnos inscriptos activos.");
        }

        LocalDateTime fIniInsc = LocalDateTime.parse(fechaInicioInscripcion.contains("T") ? fechaInicioInscripcion : fechaInicioInscripcion + "T00:00:00");
        LocalDateTime fFinInsc = LocalDateTime.parse(fechaFinInscripcion.contains("T") ? fechaFinInscripcion : fechaFinInscripcion + "T23:59:59");

        if (fFinInsc.isBefore(fIniInsc)) {
            throw new ExcepcionValidacion("CU-13 Excepción paso 7: La fecha de fin de inscripción debe ser posterior a la fecha de inicio.");
        }

        cohorte.setFechaInicioInscripcion(fIniInsc);
        cohorte.setFechaFinInscripcion(fFinInsc);
        cohorte.setSemanasAcceso(semanasAcceso > 0 ? semanasAcceso : cohorte.getSemanasAcceso());
        cohorte.setCupoMaximo(cupoMaximo != null && cupoMaximo > 0 ? cupoMaximo : cohorte.getCupoMaximo());

        if (fechaInicioDictado != null && !fechaInicioDictado.isBlank()) {
            cohorte.setFechaInicioDictado(LocalDateTime.parse(fechaInicioDictado.contains("T") ? fechaInicioDictado : fechaInicioDictado + "T00:00:00"));
        }
        if (fechaFinDictado != null && !fechaFinDictado.isBlank()) {
            cohorte.setFechaFinDictado(LocalDateTime.parse(fechaFinDictado.contains("T") ? fechaFinDictado : fechaFinDictado + "T23:59:59"));
        }

        cohorte.setUltimaModificacion(LocalDateTime.now());
        return cohorteRepository.save(cohorte);
    }

    /**
     * CU-14 — Dar de baja cohorte.
     * 
     * Regla: No permite la baja si posee inscripciones activas asociadas.
     */
    @Override
    public void darDeBajaCohorte(Integer cohorteId) {
        Cohorte cohorte = cohorteRepository.findById(cohorteId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Cohorte no encontrada con ID: " + cohorteId));

        if (cohorte.getBaja()) {
            throw new ExcepcionNegocio("La cohorte ya se encuentra cancelada.");
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByCohorte(cohorte);
        boolean tieneInscripciones = inscripciones.stream().anyMatch(i -> !i.getBaja());
        if (tieneInscripciones) {
            throw new ExcepcionConflicto("CU-14 Excepción paso 2: No se puede cancelar la cohorte porque posee alumnos inscriptos activos.");
        }

        cohorte.setBaja(true);
        cohorte.setUltimaModificacion(LocalDateTime.now());
        cohorteRepository.save(cohorte);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cohorte> buscarPorId(Integer id) {
        return cohorteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cohorte> buscarPorPrograma(Programa programa) {
        return cohorteRepository.findByProgramaAndBajaFalse(programa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cohorte> buscarCohortesConFiltros(Integer programaId, String estado, String fechaDesde, String fechaHasta) {
        List<Cohorte> lista;
        if (programaId != null) {
            Programa programa = programaRepository.findById(programaId).orElse(null);
            if (programa == null) return List.of();
            lista = cohorteRepository.findByProgramaAndBajaFalse(programa);
        } else {
            if ("historicos".equalsIgnoreCase(estado) || "inactivas".equalsIgnoreCase(estado)) {
                lista = cohorteRepository.findAll();
            } else {
                lista = cohorteRepository.findByBajaFalse();
            }
        }
        return lista;
    }
}
