package com.app.idoneos.service.modulo_cursos;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Servicio para la gestión pedagógica y comercial del catálogo de cursos.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-01 — Buscar curso: búsqueda con filtros avanzados multicriterio.
 *   CU-02 — Ver mis cursos: consulta de cursos matriculados por un alumno.
 *   CU-03 — Registrar curso: alta de cursos con docentes y supervisores asociados.
 *   CU-04 — Modificar curso: edición de metadatos, imagen y arancel.
 *   CU-05 — Dar de baja curso: baja lógica con validación de programas y cohortes vigentes.
 *   CU-06 — Explorar catálogo de cursos: listado público de cursos disponibles.
 */
@Service
@Transactional
public class CursoServiceImpl implements CursoService {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    @Override
    public Curso guardar(Curso curso) {
        return registrarCurso(curso);
    }

    /**
     * CU-01 — Buscar curso.
     * Recupera cursos publicados aplicando filtros multicriterio por nombre, categoría y modalidad.
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
     * CU-02 — Registrar curso.
     * 
     * Valida precondiciones y reglas de negocio del registro de curso:
     * - Campos obligatorios no vacíos (Excepción CU-02, paso 4).
     * - Precio no menor a cero (Excepción CU-02, paso 5).
     * - El curso se crea inicialmente en estado NO publicado.
     */
    @Override
    public Curso registrarCurso(Curso curso) {
        // CU-02 - Excepción, paso 4: Campos obligatorios
        if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-02 Excepción paso 4: El nombre del curso es obligatorio.");
        }
        if (curso.getCategoria() == null) {
            throw new ExcepcionValidacion("CU-02 Excepción paso 4: La categoría del curso es obligatoria.");
        }

        // CU-02 - Excepción, paso 5: Precio >= 0
        if (curso.getPrecio() < 0) {
            throw new ExcepcionValidacion("CU-02 Excepción paso 5: El precio del curso no puede ser menor a cero.");
        }

        curso.setPublicado(false);
        curso.setBaja(false);
        curso.setFechaCreacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    /**
     * CU-03 — Modificar curso.
     * 
     * Reglas de negocio:
     * - Valida campos obligatorios y precio >= 0 (Excepción CU-03, pasos 4 y 5).
     * - Actualiza la fecha de última modificación.
     */
    @Override
    public Curso modificarCurso(Curso curso) {
        Curso existente = cursoRepository.findById(curso.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Curso", "id", curso.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-03 Precondición: No se puede modificar un curso que se encuentra dado de baja.");
        }

        if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-03 Excepción paso 4: El nombre del curso no puede quedar vacío.");
        }

        if (curso.getPrecio() < 0) {
            throw new ExcepcionValidacion("CU-03 Excepción paso 5: El precio del curso no puede ser menor a cero.");
        }

        existente.setNombre(curso.getNombre());
        existente.setDescripcion(curso.getDescripcion());
        existente.setPrecio(curso.getPrecio());
        existente.setCategoria(curso.getCategoria());
        existente.setUltimaModificacion(LocalDateTime.now());

        return cursoRepository.save(existente);
    }

    /**
     * CU-03 — Publicar / Despublicar curso.
     * 
     * Regla de Negocio de Publicación:
     * Para publicar un curso, este debe poseer al menos 1 programa cargado,
     * el cual debe contener al menos 10 unidades temáticas y cada unidad debe
     * contar con al menos un material de estudio publicado.
     */
    @Override
    public Curso cambiarEstadoPublicacion(int cursoId, boolean publicar) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Curso", "id", cursoId));

        if (curso.getBaja()) {
            throw new ExcepcionValidacion("CU-03: No se puede publicar un curso dado de baja.");
        }

        if (publicar) {
            List<Programa> programas = programaRepository.findByCurso(curso);
            if (programas.isEmpty()) {
                throw new ExcepcionValidacion("CU-03 Regla de Publicación: El curso debe tener al menos un programa asignado.");
            }

            Programa programaPrincipal = programas.get(0);
            List<Unidad> unidades = unidadRepository.findByPrograma(programaPrincipal);
            if (unidades.size() < 10) {
                throw new ExcepcionValidacion("CU-03 Regla de Publicación: El programa debe contener al menos 10 unidades cargadas para ser publicado. Actuales: " + unidades.size());
            }

            for (Unidad u : unidades) {
                List<Material> materiales = materialRepository.findByUnidad(u);
                boolean tieneMaterialPublicado = materiales.stream().anyMatch(m -> m.getPublicado() && !m.getBaja());
                if (!tieneMaterialPublicado) {
                    throw new ExcepcionValidacion("CU-03 Regla de Publicación: La unidad '" + u.getTitulo() + "' debe poseer al menos un material de estudio publicado.");
                }
            }
        }

        curso.setPublicado(publicar);
        curso.setUltimaModificacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    /**
     * CU-04 — Eliminar curso (Baja Lógica).
     * 
     * Reglas de Negocio:
     * - Valida existencia del curso.
     * - Impide la baja si existen cohortes activas con inscripciones vigentes de alumnos.
     * - Marca baja = true y retira el curso del catálogo público (publicado = false).
     */
    @Override
    public void darDeBajaCurso(int cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Curso", "id", cursoId));

        if (curso.getBaja()) {
            throw new ExcepcionValidacion("CU-04 Excepción: El curso ya se encuentra dado de baja.");
        }

        // CU-04: Verificación de dependencias con cohortes e inscripciones vigentes
        List<Programa> programas = programaRepository.findByCurso(curso);
        for (Programa p : programas) {
            List<Cohorte> cohortes = p.getCohortes();
            if (cohortes != null) {
                for (Cohorte c : cohortes) {
                    List<Inscripcion> inscripciones = inscripcionRepository.findByCohorte(c);
                    boolean tieneInscripcionesActivas = inscripciones.stream().anyMatch(i -> !i.getBaja());
                    if (tieneInscripcionesActivas) {
                        throw new ExcepcionValidacion("CU-04 Excepción paso 5: No se puede dar de baja el curso porque posee cohortes activas con alumnos inscriptos.");
                    }
                }
            }
        }

        curso.setBaja(true);
        curso.setPublicado(false);
        curso.setUltimaModificacion(LocalDateTime.now());
        cursoRepository.save(curso);
    }

    /**
     * CU-05 — Explorar catálogo de cursos.
     * Retorna únicamente cursos publicados y no dados de baja.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerPublicados() {
        return cursoRepository.findByBajaFalseAndPublicadoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Curso> buscarPorId(Integer id) {
        return cursoRepository.findById(id).filter(c -> !c.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerTodo() {
        return cursoRepository.findByBajaFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerPorCategoria(Categoria categoria) {
        return cursoRepository.findByCategoriaAndBajaFalseAndPublicadoTrue(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> obtenerPorDocente(Usuario usuario) {
        // Usa el id del Docente (id_docente), NO el id del Usuario (id_usuario).
        // Son secuencias independientes: confundirlos devuelve los cursos del docente incorrecto.
        if (usuario.getDocente() != null) {
            return cursoRepository.findByDocenteId(usuario.getDocente().getId());
        }
        return java.util.Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> buscarPorNombre(String query) {
        return cursoRepository.findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(query);
    }

    @Override
    public Curso modificar(Curso curso) {
        return modificarCurso(curso);
    }

    @Override
    public void borrar(Curso curso) {
        darDeBajaCurso(curso.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return cursoRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}

