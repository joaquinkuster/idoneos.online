package com.app.idoneos.service.Inscripcion;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.InscripcionRepository;
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
    public List<Inscripcion> obtenerPorAlumno(Usuario usuario) {
        return inscripcionRepository.findByUsuarioAndBajaFalse(usuario);
    }

    @Override
    public Optional<Inscripcion> obtenerPorAlumnoYCurso(Usuario usuario, Curso curso) {
        return inscripcionRepository.findByUsuarioAndCursoAndBajaFalse(usuario, curso);
    }

    @Override
    public boolean estaInscripto(Usuario usuario, Curso curso) {
        return inscripcionRepository.existsByUsuarioAndCursoAndBajaFalse(usuario, curso);
    }

    @Override
    public Inscripcion inscribirAlumno(Usuario usuario, Curso curso) {
        if (estaInscripto(usuario, curso)) {
            return obtenerPorAlumnoYCurso(usuario, curso).orElseThrow();
        }
        Inscripcion nueva = new Inscripcion(usuario, curso);
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
