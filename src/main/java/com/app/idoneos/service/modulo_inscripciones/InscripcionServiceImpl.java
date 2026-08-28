package com.app.idoneos.service.modulo_inscripciones;

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

@Service
@Transactional
public class InscripcionServiceImpl implements InscripcionService {

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private ProgresoService progresoService;

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
    public List<Inscripcion> obtenerPorCohorte(Cohorte cohorte) {
        return inscripcionRepository.findByCohorte(cohorte).stream().filter(i -> !i.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaInscripto(Usuario usuario, Curso curso) {
        return inscripcionRepository.existsByUsuarioAndCursoAndBajaFalse(usuario, curso);
    }

    @Override
    public Inscripcion inscribirAlumno(Usuario usuario, Curso curso) {
        if (usuario == null || curso == null) {
            throw new ExcepcionValidacion("CU-44 Excepción: El usuario y el curso son obligatorios.");
        }

        if (estaInscripto(usuario, curso)) {
            throw new ExcepcionValidacion("CU-44 Excepción: El usuario ya se encuentra inscripto en el curso.");
        }

        List<Programa> programas = programaRepository.findByCursoAndBajaFalse(curso);
        if (programas.isEmpty()) {
            throw new ExcepcionNegocio("CU-44 Excepción: El curso no posee un programa activo para inscribirse.");
        }

        List<Cohorte> cohortes = cohorteRepository.findByProgramaAndBajaFalse(programas.get(0));
        Cohorte cohorte = cohortes.isEmpty() ? null : cohortes.get(0);

        if (cohorte == null) {
            LocalDateTime ahora = LocalDateTime.now();
            cohorte = new Cohorte(ahora, ahora.plusMonths(3), 12, programas.get(0));
            cohorte = cohorteRepository.save(cohorte);
        }

        return inscribirAlumnoACohorte(usuario, cohorte);
    }

    @Override
    public Inscripcion inscribirAlumnoACohorte(Usuario usuario, Cohorte cohorte) {
        if (usuario == null || cohorte == null) {
            throw new ExcepcionValidacion("CU-44 Excepción: Usuario y cohorte obligatorios.");
        }

        Curso curso = cohorte.getPrograma() != null ? cohorte.getPrograma().getCurso() : null;
        if (curso != null && estaInscripto(usuario, curso)) {
            throw new ExcepcionValidacion("CU-44 Excepción: El usuario ya está matriculado en este curso.");
        }

        Alumno alumno = alumnoRepository.findByUsuario(usuario)
                .orElseGet(() -> alumnoRepository.save(new Alumno(usuario)));

        // Validar cupo
        if (cohorte.getCupoMaximo() != null && cohorte.getCupoMaximo() > 0) {
            long inscriptosActivos = inscripcionRepository.findByCohorte(cohorte).stream().filter(i -> !i.getBaja()).count();
            if (inscriptosActivos >= cohorte.getCupoMaximo()) {
                throw new ExcepcionConflicto("CU-44 Excepción: La cohorte seleccionada ha alcanzado su cupo máximo de alumnos.");
            }
        }

        Inscripcion inscripcion = new Inscripcion(cohorte, alumno);
        inscripcion.setBaja(false);
        inscripcion.setFecha(LocalDateTime.now());
        inscripcion.setFechaVencimientoAcceso(LocalDateTime.now().plusWeeks(cohorte.getSemanasAcceso()));
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        // CU-44 paso 6: Registrar progreso inicial para la primera unidad del cronograma
        progresoService.registrarProgresoInicial(guardada);

        return guardada;
    }

    @Override
    public void darDeBajaInscripcion(int inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Inscripcion", "id", inscripcionId));

        if (inscripcion.getBaja()) {
            throw new ExcepcionValidacion("La inscripción ya se encuentra dada de baja.");
        }

        inscripcion.setBaja(true);
        inscripcionRepository.save(inscripcion);
    }

    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    public void borrar(Inscripcion entidad) {
        darDeBajaInscripcion(entidad.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return inscripcionRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
