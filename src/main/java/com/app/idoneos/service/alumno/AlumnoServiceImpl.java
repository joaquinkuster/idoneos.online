package com.app.idoneos.service.alumno;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.util.TextoUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de AlumnoService. */
@Service
public class AlumnoServiceImpl implements AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el alumno por su identificador. */
    @Override
    public Optional<Alumno> buscarPorId(Integer id) {
        return alumnoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Alumno> obtenerTodo() {
        return alumnoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return alumnoRepository.existsById(id);
    }

    @Override
    public List<Alumno> buscarPorCurso(Curso curso) {
        return ordenarPorApellidoYNombre(alumnoRepository.buscarPorCurso(curso));
    }

    @Override
    public List<Alumno> buscarPorCohorte(Cohorte cohorte) {
        return ordenarPorApellidoYNombre(alumnoRepository.buscarPorCohorte(cohorte));
    }

    private List<Alumno> ordenarPorApellidoYNombre(List<Alumno> alumnos) {
        return alumnos.stream()
                .sorted(Comparator.comparing((Alumno a) -> TextoUtil.normalizar(a.getUsuario().getApellido()))
                        .thenComparing(a -> TextoUtil.normalizar(a.getUsuario().getNombre())))
                .toList();
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el alumno en la base de datos. */
    @Override
    public Alumno guardar(Alumno entidad) {
        return alumnoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Alumno modificar(Alumno entidad) {
        return alumnoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Alumno entidad) {
        alumnoRepository.delete(entidad);
    }
}
