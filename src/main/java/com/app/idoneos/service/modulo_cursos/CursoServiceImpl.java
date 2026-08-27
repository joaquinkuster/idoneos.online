package com.app.idoneos.service.modulo_cursos;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TRAZABILIDAD — Implementación del Servicio para el Módulo de Cursos (MOD-F-01).
 *
 * Implementa las operaciones del sistema y reglas de negocio detalladas en:
 * - Contratos.md: buscarCursos, registrarCurso, modificarCurso, darDeBajaCurso.
 * - DSS.md: flujos de interacción del actor con el sistema.
 * - Casos de Uso Reales.md: CU-01 a CU-06.
 */
@Service
@Transactional
public class CursoServiceImpl implements CursoService {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private SupervisorRepository supervisorRepository;
    @Autowired private NivelRepository nivelRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    @Override
    public Curso guardar(Curso curso) {
        return registrarCurso(curso);
    }

    @Override
    public Curso modificar(Curso curso) {
        curso.setUltimaModificacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    /**
     * CU-01 & CU-06 — Búsqueda de cursos publicados con filtros multicriterio.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Curso> buscarCursosPublicadosConFiltros(String nombre, Integer categoriaId, Integer modalidadId) {
        return cursoRepository.buscarCursosPublicadosConFiltros(
                (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null,
                categoriaId,
                modalidadId
        );
    }

    /**
     * CU-01 — Búsqueda administrativa con filtros avanzados para Docente y Administrador.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Curso> buscarCursosAdminConFiltros(String busqueda, Integer categoriaId, Integer nivelId, Integer docenteId, Boolean publicado) {
        List<Curso> lista = cursoRepository.findByBajaFalse();
        return lista.stream()
                .filter(c -> busqueda == null || busqueda.isBlank() ||
                        c.getNombre().toLowerCase().contains(busqueda.toLowerCase()) ||
                        (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(busqueda.toLowerCase())))
                .filter(c -> categoriaId == null || (c.getCategoria() != null && c.getCategoria().getId() == categoriaId))
                .filter(c -> nivelId == null || (c.getNivel() != null && c.getNivel().getId() == nivelId))
                .filter(c -> docenteId == null || (c.getDocente() != null && c.getDocente().getId() == docenteId))
                .filter(c -> publicado == null || Boolean.valueOf(c.getPublicado()).equals(publicado))
                .collect(Collectors.toList());
    }

    /**
     * CU-03 — Registrar curso (Admin).
     * 
     * Reglas y validaciones de Contratos.md / Casos de Uso Reales.md:
     * - Campos obligatorios: nombre, categoría, nivel, docente titular.
     * - Precio >= 0.
     * - El docente titular no puede ser supervisor a la vez en el mismo curso.
     * - El curso se registra inicialmente sin cohortes y en estado no publicado.
     */
    @Override
    public Curso registrarCurso(Curso curso) {
        if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-03 Excepción paso 4: El nombre del curso es obligatorio.");
        }
        if (curso.getCategoria() == null) {
            throw new ExcepcionValidacion("CU-03 Excepción paso 4: La categoría del curso es obligatoria.");
        }
        if (curso.getPrecio() < 0) {
            throw new ExcepcionValidacion("CU-03 Excepción paso 5: El precio del curso no puede ser menor a cero.");
        }
        if (curso.getDocente() == null) {
            throw new ExcepcionValidacion("CU-03 Excepción paso 4: Debe asignarse un docente titular habilitado.");
        }

        curso.setPublicado(false);
        curso.setBaja(false);
        curso.setFechaCreacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    @Override
    public Curso registrarCursoConEquipo(Curso curso, Integer docenteSupervisorId) {
        Curso guardado = registrarCurso(curso);
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(guardado.getDocente().getId())) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup -> {
                Supervisor s = new Supervisor(guardado, sup);
                supervisorRepository.save(s);
            });
        }
        return guardado;
    }

    /**
     * CU-04 — Modificar curso (Admin).
     * 
     * Reglas:
     * - Si posee inscripciones activas asociadas, se bloquea la modificación de datos académicos
     *   críticos (solo se permite precio e imagen de portada).
     * - Si no posee inscripciones activas, se pueden modificar todos los campos.
     */
    @Override
    public Curso modificarCurso(Integer cursoId, String nombre, String descripcion, float precio, Integer categoriaId,
                                Integer docenteTitularId, Integer docenteSupervisorId, Integer nivelId, boolean emiteCertificado) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Curso no encontrado con ID: " + cursoId));

        if (curso.getBaja()) {
            throw new ExcepcionNegocio("CU-04 Excepción paso 4: No se puede modificar un curso dado de baja.");
        }

        if (precio < 0) {
            throw new ExcepcionValidacion("CU-04 Excepción paso 6: El precio no puede ser menor a cero.");
        }

        // Verificar si existen inscripciones activas
        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoAndBajaFalse(curso);
        boolean tieneInscripcionesActivas = !inscripciones.isEmpty();

        if (tieneInscripcionesActivas) {
            // Solo permite actualizar precio y certificado/imagen
            curso.setPrecio(precio);
            curso.setEmiteCertificado(emiteCertificado);
        } else {
            // Actualización completa
            if (nombre != null && !nombre.isBlank()) curso.setNombre(nombre.trim());
            curso.setDescripcion(descripcion);
            curso.setPrecio(precio);
            curso.setEmiteCertificado(emiteCertificado);

            if (categoriaId != null) {
                categoriaRepository.findById(categoriaId).ifPresent(curso::setCategoria);
            }
            if (nivelId != null) {
                nivelRepository.findById(nivelId).ifPresent(curso::setNivel);
            }
            if (docenteTitularId != null) {
                docenteRepository.findById(docenteTitularId).ifPresent(curso::setDocente);
            }

            // Actualizar supervisores
            List<Supervisor> supervisoresActuales = supervisorRepository.findByCurso(curso);
            supervisorRepository.deleteAll(supervisoresActuales);

            if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
                docenteRepository.findById(docenteSupervisorId).ifPresent(sup -> {
                    Supervisor s = new Supervisor(curso, sup);
                    supervisorRepository.save(s);
                });
            }
        }

        curso.setUltimaModificacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    /**
     * CU-05 — Dar de baja curso (Admin).
     * 
     * Regla: No permite la baja si tiene programas activos asociados.
     */
    @Override
    public void darDeBajaCurso(Integer idCurso) {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Curso no encontrado con ID: " + idCurso));

        if (curso.getBaja()) {
            throw new ExcepcionNegocio("El curso ya se encuentra dado de baja.");
        }

        List<Programa> programas = programaRepository.findByCurso(curso);
        boolean tieneProgramasActivos = programas.stream().anyMatch(p -> !p.isBaja());
        if (tieneProgramasActivos) {
            throw new ExcepcionConflicto("CU-05 Excepción paso 2: No se puede dar de baja el curso porque tiene programas activos asociados.");
        }

        curso.setBaja(true);
        curso.setPublicado(false);
        curso.setUltimaModificacion(LocalDateTime.now());
        cursoRepository.save(curso);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Curso> buscarPorId(int id) {
        return cursoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> buscarPorNombre(String nombre) {
        return cursoRepository.findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerPorCategoria(Categoria categoria) {
        return cursoRepository.findByCategoriaAndBajaFalseAndPublicadoTrue(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerPublicados() {
        return cursoRepository.findByBajaFalseAndPublicadoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerTodo() {
        return cursoRepository.findByBajaFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerPorDocente(Usuario usuario) {
        if (usuario != null && usuario.getDocente() != null) {
            return cursoRepository.findByDocenteId(usuario.getDocente().getId());
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> buscarCursosPorDocente(int docenteId) {
        return cursoRepository.findByDocenteId(docenteId);
    }
}
