package com.app.idoneos.servicio.Alumno;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Cohorte;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el alumno.
 */
public interface AlumnoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el perfil de alumno asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Alumno> buscarPorUsuario(Usuario usuario);

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
