package com.app.idoneos.service.Programa;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Dictado;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Curso;
import com.app.idoneos.repository.DictadoRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.ProgramaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de servicios para la gestión de programas de estudio (CU-10 a CU-14).
 */
@Service
@Transactional
public class ProgramaServiceImpl implements ProgramaService {

    @Autowired private ProgramaRepository programaRepository;
    @Autowired private DictadoRepository dictadoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Programa> buscarPorId(Integer id) {
        return programaRepository.findById(id).filter(p -> !p.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> obtenerTodo() {
        return programaRepository.findAll().stream().filter(p -> !p.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> obtenerPorCurso(Curso curso) {
        return programaRepository.findByCurso(curso).stream().filter(p -> !p.getBaja()).toList();
    }

    /**
     * CU-11 — Registrar programa.
     * Reglas de negocio:
     * - Nombre obligatorio (Excepción CU-11, paso 4).
     * - Curso asociado obligatorio.
     * - Meses de acceso mayores a cero (Excepción CU-11, paso 5).
     */
    @Override
    public Programa registrarPrograma(Programa programa) {
        if (programa.getNombre() == null || programa.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-11 Excepción paso 4: El nombre del programa es obligatorio.");
        }
        if (programa.getCurso() == null) {
            throw new ExcepcionValidacion("CU-11 Excepción paso 4: El curso asociado es obligatorio.");
        }
        if (programa.getMesesAcceso() <= 0) {
            throw new ExcepcionValidacion("CU-11 Excepción paso 5: Los meses de acceso deben ser mayores a cero.");
        }

        programa.setBaja(false);
        return programaRepository.save(programa);
    }

    /**
     * CU-12 — Modificar programa.
     * Reglas de negocio:
     * - Programa existente y activo.
     * - Nombre obligatorio y meses de acceso > 0.
     */
    @Override
    public Programa modificarPrograma(Programa programa) {
        Programa existente = programaRepository.findById(programa.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Programa", "id", programa.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-12 Precondición: No se puede modificar un programa dado de baja.");
        }
        if (programa.getNombre() == null || programa.getNombre().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-12 Excepción paso 4: El nombre del programa no puede quedar vacío.");
        }
        if (programa.getMesesAcceso() <= 0) {
            throw new ExcepcionValidacion("CU-12 Excepción paso 5: Los meses de acceso deben ser mayores a cero.");
        }

        existente.setNombre(programa.getNombre().trim());
        existente.setDescripcion(programa.getDescripcion());
        existente.setMesesAcceso(programa.getMesesAcceso());
        return programaRepository.save(existente);
    }

    /**
     * CU-13 — Eliminar programa (Baja Lógica).
     * Impide la baja si existen dictados activos con inscripciones vigentes de alumnos.
     */
    @Override
    public void darDeBajaPrograma(int programaId) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Programa", "id", programaId));

        if (programa.getBaja()) {
            throw new ExcepcionValidacion("CU-13 Excepción: El programa ya se encuentra dado de baja.");
        }

        List<Dictado> dictados = dictadoRepository.findByPrograma(programa);
        for (Dictado d : dictados) {
            List<Inscripcion> inscripciones = inscripcionRepository.findByDictado(d);
            boolean tieneInscripcionesActivas = inscripciones.stream().anyMatch(i -> !i.getBaja());
            if (tieneInscripcionesActivas) {
                throw new ExcepcionValidacion("CU-13 Excepción paso 5: No se puede dar de baja el programa porque posee dictados activos con alumnos inscriptos.");
            }
        }

        programa.setBaja(true);
        programaRepository.save(programa);
    }

    /**
     * CU-14 — Cambiar programa activo del curso.
     */
    @Override
    public Programa cambiarProgramaActivo(int programaId) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Programa", "id", programaId));

        if (programa.getBaja()) {
            throw new ExcepcionValidacion("CU-14 Excepción: No se puede activar un programa que se encuentra dado de baja.");
        }
        return programa;
    }

    @Override
    public Programa guardar(Programa programa) {
        return registrarPrograma(programa);
    }

    @Override
    public Programa modificar(Programa programa) {
        return modificarPrograma(programa);
    }

    @Override
    public void borrar(Programa programa) {
        darDeBajaPrograma(programa.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return programaRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
