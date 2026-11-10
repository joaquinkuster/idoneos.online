package com.app.idoneos.service.alumno;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import java.util.List;

/** Servicio del alumno. */
public interface AlumnoService extends CrudService<Alumno> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los alumnos con una inscripción activa al curso, ordenados por apellido y nombre.
     *
     * @param curso el curso
     * @return los alumnos del curso
     */
    List<Alumno> buscarPorCurso(Curso curso);

    /**
     * Busca los alumnos con una inscripción activa a la cohorte, ordenados por apellido y nombre.
     *
     * @param cohorte la cohorte
     * @return los alumnos de la cohorte
     */
    List<Alumno> buscarPorCohorte(Cohorte cohorte);
}
