package com.app.idoneos.service.Inscripcion;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.DictadoRepository;
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
    @Autowired private DictadoRepository dictadoRepository;
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

        Alumno alumno = alumnoRepository.findById(usuario.getId())
                .orElseGet(() -> alumnoRepository.save(new Alumno(usuario)));

        Inscripcion nueva = new Inscripcion(usuario, curso);
        nueva.setAlumno(alumno);
        nueva.setBaja(false);
        nueva.setFecha(LocalDateTime.now());
        nueva.setFechaVencimientoAcceso(LocalDateTime.now().plusMonths(curso.getMesesAcceso() > 0 ? curso.getMesesAcceso() : 12));
        return inscripcionRepository.save(nueva);
    }

    /**
     * CU-42 — Inscribir alumno a dictado con control de cupo máximo.
     */
    public Inscripcion inscribirAlumnoADictado(Usuario usuario, Dictado dictado) {
        if (dictado == null || dictado.getBaja()) {
            throw new ExcepcionValidacion("CU-42 Precondición: El dictado debe estar activo.");
        }

        Curso curso = dictado.getPrograma() != null ? dictado.getPrograma().getCurso() : null;
        if (curso != null && estaInscripto(usuario, curso)) {
            throw new ExcepcionValidacion("CU-42 Excepción paso 4: El alumno ya se encuentra inscripto en este curso.");
        }

        List<Inscripcion> inscriptosDictado = inscripcionRepository.findByDictado(dictado);
        long activos = inscriptosDictado.stream().filter(i -> !i.getBaja()).count();
        if (dictado.getCupoMaximo() > 0 && activos >= dictado.getCupoMaximo()) {
            throw new ExcepcionValidacion("CU-42 Excepción paso 5: El dictado ha alcanzado su cupo máximo disponible (" + dictado.getCupoMaximo() + ").");
        }

        Alumno alumno = alumnoRepository.findById(usuario.getId())
                .orElseGet(() -> alumnoRepository.save(new Alumno(usuario)));

        Inscripcion nueva = new Inscripcion(alumno, dictado);
        nueva.setBaja(false);
        nueva.setFecha(LocalDateTime.now());
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
        if (entidad.getAlumno() != null && entidad.getAlumno().getUsuario() != null && entidad.getCurso() != null) {
            return inscribirAlumno(entidad.getAlumno().getUsuario(), entidad.getCurso());
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
