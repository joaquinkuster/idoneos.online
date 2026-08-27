package com.app.idoneos.service.modulo_inscripciones;

import com.app.idoneos.model.*;
import com.app.idoneos.service.modulo_configuracion.CrudService;
import java.util.List;
import java.util.Optional;

public interface InscripcionService extends CrudService<Inscripcion> {

    List<Inscripcion> obtenerPorAlumno(Usuario alumno);

    Optional<Inscripcion> obtenerPorAlumnoYCurso(Usuario alumno, Curso curso);

    List<Inscripcion> obtenerPorCohorte(Cohorte cohorte);

    boolean estaInscripto(Usuario alumno, Curso curso);

    Inscripcion inscribirAlumno(Usuario alumno, Curso curso);

    Inscripcion inscribirAlumnoACohorte(Usuario alumno, Cohorte cohorte);

    void darDeBajaInscripcion(int inscripcionId);
}
