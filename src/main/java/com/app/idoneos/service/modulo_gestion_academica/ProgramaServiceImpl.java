package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProgramaServiceImpl implements ProgramaService {

    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private CohorteRepository cohorteRepository;

    @Override
    public Programa registrarPrograma(Integer cursoId, String nombre, String descripcion, String version) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Curso no encontrado con ID: " + cursoId));

        if (curso.getBaja()) {
            throw new ExcepcionNegocio("CU-16 Excepción: No se puede registrar un programa en un curso dado de baja.");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-16 Excepción: El nombre del programa es obligatorio.");
        }

        Programa p = new Programa(nombre.trim(), descripcion, "Objetivos generales", "Bibliografía general", curso);
        p.setBaja(false);
        p.setFechaCreacion(LocalDateTime.now());
        return programaRepository.save(p);
    }

    @Override
    public Programa modificarPrograma(Integer programaId, String nombre, String descripcion, String version) {
        Programa p = programaRepository.findById(programaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Programa no encontrado con ID: " + programaId));

        if (p.isBaja()) {
            throw new ExcepcionNegocio("CU-17 Excepción: No se puede modificar un programa dado de baja.");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-17 Excepción: El nombre del programa no puede estar vacío.");
        }

        p.setNombre(nombre.trim());
        p.setDescripcion(descripcion);
        p.setUltimaModificacion(LocalDateTime.now());
        return programaRepository.save(p);
    }

    @Override
    public void darDeBajaPrograma(Integer programaId) {
        Programa p = programaRepository.findById(programaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Programa no encontrado con ID: " + programaId));

        if (p.isBaja()) {
            throw new ExcepcionNegocio("El programa ya se encuentra dado de baja.");
        }

        List<Cohorte> cohortes = cohorteRepository.findByProgramaAndBajaFalse(p);
        if (!cohortes.isEmpty()) {
            throw new ExcepcionConflicto("CU-18 Excepción: No se puede dar de baja el programa porque tiene cohortes activas asociadas.");
        }

        p.setBaja(true);
        p.setUltimaModificacion(LocalDateTime.now());
        programaRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Programa> buscarPorId(Integer id) {
        return programaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> buscarPorCurso(Curso curso) {
        return programaRepository.findByCurso(curso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> buscarProgramasConFiltros(Integer cursoId, String busqueda, Boolean baja) {
        List<Programa> lista = (baja != null && baja) ? programaRepository.findAll() : programaRepository.findAll().stream().filter(p -> !p.isBaja()).toList();
        return lista.stream()
                .filter(p -> cursoId == null || (p.getCurso() != null && p.getCurso().getIdCurso() == cursoId))
                .filter(p -> busqueda == null || busqueda.isBlank() ||
                        p.getNombre().toLowerCase().contains(busqueda.toLowerCase()) ||
                        (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(busqueda.toLowerCase())))
                .collect(Collectors.toList());
    }
}
