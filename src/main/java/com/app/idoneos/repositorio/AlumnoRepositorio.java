package com.app.idoneos.repositorio;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Cohorte;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Alumno} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Alumno}.
 */
@Repository
public interface AlumnoRepositorio extends JpaRepository<Alumno, Integer> {

    /**
     * Busca el perfil de alumno asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Alumno> findByUsuario(Usuario usuario);

    /**
     * Busca los alumnos con una inscripción activa a alguna cohorte activa del curso, sin repetirlos.
     *
     * @param curso el curso
     * @return los alumnos inscriptos al curso
     */
    @Query("SELECT DISTINCT a FROM Alumno a JOIN a.inscripciones i WHERE i.baja = false AND i.cohorte.baja = false "
            + "AND i.cohorte.programa.baja = false AND i.cohorte.programa.curso = :curso")
    List<Alumno> buscarActivosPorCurso(@Param("curso") Curso curso);

    /**
     * Busca los alumnos con una inscripción activa a la cohorte.
     *
     * @param cohorte la cohorte
     * @return los alumnos inscriptos a la cohorte
     */
    @Query("SELECT DISTINCT a FROM Alumno a JOIN a.inscripciones i WHERE i.baja = false AND i.cohorte = :cohorte")
    List<Alumno> buscarActivosPorCohorte(@Param("cohorte") Cohorte cohorte);
}
