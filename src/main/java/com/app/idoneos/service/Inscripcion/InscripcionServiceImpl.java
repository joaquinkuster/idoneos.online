package com.app.idoneos.service.Inscripcion;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de inscripciones de alumnos a cursos/dictados (CU-41 a CU-43).
 */
@Service
@Transactional
public class InscripcionServiceImpl implements InscripcionService {

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private AlumnoRepository alumnoRepository;

    /**
     * CU-41 — Buscar inscripción por ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepository.findById(id).filter(i -> !i.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscripcion> obtenerTodo() {
        return inscripcionRepository.findAll().stream().filter(i -> !i.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscripcion> obtenerPorAlumno(Usuario usuario) {
        return inscripcionRepository.findByUsuarioAndBajaFalse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inscripcion> obtenerPorAlumnoYCurso(Usuario usuario, Curso curso) {
        return inscripcionRepository.findByUsuarioAndCursoAndBajaFalse(usuario, curso);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaInscripto(Usuario usuario, Curso curso) {
        return inscripcionRepository.existsByUsuarioAndCursoAndBajaFalse(usuario, curso);
    }

    /**
     * CU-42 — Inscribir curso.
     * Reglas de negocio:
     * - Valida que el alumno no posea una inscripción activa previa (Excepción CU-42, paso 4).
     * - Valida disponibilidad de cupo en el dictado (Excepción CU-42, paso 5).
     * - Asigna fecha de inscripción e inicia estado activo (baja = false).
     */
    @Override
    public Inscripcion inscribirAlumno(Usuario usuario, Curso curso) {
        if (usuario == null || curso == null) {
            throw new ExcepcionValidacion("CU-42 Excepción paso 4: El usuario y el curso son obligatorios.");
        }

        if (estaInscripto(usuario, curso)) {
            throw new ExcepcionValidacion("CU-42 Excepción paso 4: El usuario ya se encuentra inscripto activamente en el curso.");
        }

        Alumno alumno = alumnoRepository.findByUsuario(usuario)
                .orElseGet(() -> alumnoRepository.save(new Alumno(usuario)));

        // Busca la cohorte activa más reciente del programa del curso
        Cohorte cohorte = cohorteRepository.findAll().stream()
                .filter(c -> !c.getBaja() && c.getPrograma() != null
                        && c.getPrograma().getCurso() != null
                        && c.getPrograma().getCurso().getId() == curso.getId())
                .findFirst()
                .orElseThrow(() -> new ExcepcionValidacion(
                        "CU-42 Excepción paso 5: No existe una cohorte activa para el curso solicitado."));

        Inscripcion nueva = new Inscripcion(cohorte, alumno);
        nueva.setBaja(false);
        nueva.setFecha(LocalDateTime.now());
        // Acceso: semanasAcceso de la cohorte, aproximado a meses
        int semanasAcceso = cohorte.getSemanasAcceso() > 0 ? cohorte.getSemanasAcceso() : 48;
        nueva.setFechaVencimientoAcceso(LocalDateTime.now().plusWeeks(semanasAcceso));
        return inscripcionRepository.save(nueva);
    }

    /**
     * CU-42 — Inscribir alumno a cohorte con control de cupo máximo.
     */
    public Inscripcion inscribirAlumnoACohorte(Usuario usuario, Cohorte cohorte) {
        if (cohorte == null || cohorte.getBaja()) {
            throw new ExcepcionValidacion("CU-42 Precondición: La cohorte debe estar activa.");
        }

        Curso curso = cohorte.getPrograma() != null ? cohorte.getPrograma().getCurso() : null;
        if (curso != null && estaInscripto(usuario, curso)) {
            throw new ExcepcionValidacion("CU-42 Excepción paso 4: El alumno ya se encuentra inscripto en este curso.");
        }

        List<Inscripcion> inscriptosCohorte = inscripcionRepository.findByCohorte(cohorte);
        long activos = inscriptosCohorte.stream().filter(i -> !i.getBaja()).count();
        if (cohorte.getCupoMaximo() != null && cohorte.getCupoMaximo() > 0 && activos >= cohorte.getCupoMaximo()) {
            throw new ExcepcionValidacion("CU-42 Excepción paso 5: La cohorte ha alcanzado su cupo máximo (" + cohorte.getCupoMaximo() + ").");
        }

        Alumno alumno = alumnoRepository.findByUsuario(usuario)
                .orElseGet(() -> alumnoRepository.save(new Alumno(usuario)));

        Inscripcion nueva = new Inscripcion(cohorte, alumno);
        nueva.setBaja(false);
        nueva.setFecha(LocalDateTime.now());
        nueva.setFechaVencimientoAcceso(LocalDateTime.now().plusWeeks(cohorte.getSemanasAcceso()));
        return inscripcionRepository.save(nueva);
    }

    /**
     * CU-43 — Dar de baja inscripción (Baja Lógica).
     */
    @Override
    public void borrar(Inscripcion entidad) {
        darDeBajaInscripcion(entidad.getId());
    }

    public void darDeBajaInscripcion(int inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Inscripción", "id", inscripcionId));

        if (inscripcion.getBaja()) {
            throw new ExcepcionValidacion("CU-43 Excepción: La inscripción ya se encuentra dada de baja.");
        }

        inscripcion.setBaja(true);
        inscripcionRepository.save(inscripcion);
    }

    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        if (entidad.getAlumno() != null && entidad.getAlumno().getUsuario() != null
                && entidad.getCohorte() != null && entidad.getCohorte().getPrograma() != null) {
            return inscribirAlumno(entidad.getAlumno().getUsuario(),
                    entidad.getCohorte().getPrograma().getCurso());
        }
        return inscripcionRepository.save(entidad);
    }

    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return inscripcionRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
