package com.app.idoneos.service.Inscripcion;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.CrudService;

import java.util.List;
import java.util.Optional;

public interface InscripcionService extends CrudService<Inscripcion> {

    List<Inscripcion> obtenerPorAlumno(Usuario alumno);

    Optional<Inscripcion> obtenerPorAlumnoYCurso(Usuario alumno, Curso curso);

    boolean estaInscripto(Usuario alumno, Curso curso);

    Inscripcion inscribirAlumno(Usuario alumno, Curso curso);
}
