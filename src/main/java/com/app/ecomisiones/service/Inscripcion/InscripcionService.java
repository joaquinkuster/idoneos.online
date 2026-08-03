package com.app.ecomisiones.service.Inscripcion;

import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Inscripcion;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.CrudService;

import java.util.List;
import java.util.Optional;

public interface InscripcionService extends CrudService<Inscripcion> {

    List<Inscripcion> obtenerPorAlumno(Usuario alumno);

    Optional<Inscripcion> obtenerPorAlumnoYCurso(Usuario alumno, Curso curso);

    boolean estaInscripto(Usuario alumno, Curso curso);

    Inscripcion inscribirAlumno(Usuario alumno, Curso curso);
}
