package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.model.*;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Contrato de Servicio para la administración de Programas Académicos (MOD-F-02).
 *
 * MOD-F-02: Módulo de Gestión Académica
 *   CU-15 — Buscar programa
 *   CU-16 — Registrar programa
 *   CU-17 — Modificar programa
 *   CU-18 — Dar de baja programa
 */
public interface ProgramaService {

    Programa registrarPrograma(Integer cursoId, String nombre, String descripcion, String version);

    Programa modificarPrograma(Integer programaId, String nombre, String descripcion, String version);

    void darDeBajaPrograma(Integer programaId);

    Optional<Programa> buscarPorId(Integer id);

    List<Programa> buscarPorCurso(Curso curso);

    List<Programa> buscarProgramasConFiltros(Integer cursoId, String busqueda, Boolean baja);
}
