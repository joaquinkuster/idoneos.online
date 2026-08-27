package com.app.idoneos.service.modulo_cursos;

import com.app.idoneos.model.*;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Contrato de Servicio para la gestión de cohortes (MOD-F-01).
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-11 — Buscar cohorte
 *   CU-12 — Registrar cohorte
 *   CU-13 — Modificar cohorte
 *   CU-14 — Dar de baja cohorte
 */
public interface CohorteService {

    Cohorte registrarCohorte(Integer programaId, String fechaInicioInscripcion, String fechaFinInscripcion,
                             String fechaInicioDictado, String fechaFinDictado, int semanasAcceso, Integer cupoMaximo);

    Cohorte modificarCohorte(Integer cohorteId, String fechaInicioInscripcion, String fechaFinInscripcion,
                             String fechaInicioDictado, String fechaFinDictado, int semanasAcceso, Integer cupoMaximo);

    void darDeBajaCohorte(Integer cohorteId);

    Optional<Cohorte> buscarPorId(Integer id);

    List<Cohorte> buscarPorPrograma(Programa programa);

    List<Cohorte> buscarCohortesConFiltros(Integer programaId, String estado, String fechaDesde, String fechaHasta);
}
