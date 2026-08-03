package com.app.ecomisiones.service.Inscripcion;

import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Inscripcion;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InscripcionServiceImpl implements InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepository.findById(id).filter(i -> !i.getBaja());
    }

    @Override
    public List<Inscripcion> obtenerTodo() {
        return inscripcionRepository.findAll();
    }

    @Override
    public List<Inscripcion> obtenerPorAlumno(Usuario alumno) {
        return inscripcionRepository.findByAlumnoAndBajaFalse(alumno);
    }

    @Override
    public Optional<Inscripcion> obtenerPorAlumnoYCurso(Usuario alumno, Curso curso) {
        return inscripcionRepository.findByAlumnoAndCursoAndBajaFalse(alumno, curso);
    }

    @Override
    public boolean estaInscripto(Usuario alumno, Curso curso) {
        return inscripcionRepository.existsByAlumnoAndCursoAndBajaFalse(alumno, curso);
    }

    @Override
    public Inscripcion inscribirAlumno(Usuario alumno, Curso curso) {
        if (estaInscripto(alumno, curso)) {
            return obtenerPorAlumnoYCurso(alumno, curso).orElseThrow();
        }
        Inscripcion nueva = new Inscripcion(alumno, curso);
        return inscripcionRepository.save(nueva);
    }

    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    public void borrar(Inscripcion entidad) {
        entidad.setBaja(true);
        inscripcionRepository.save(entidad);
    }

    @Override
    public boolean existePorId(Integer id) {
        return inscripcionRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
