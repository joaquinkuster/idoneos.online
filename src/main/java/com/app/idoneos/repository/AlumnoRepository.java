package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Alumno} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Alumno}.
 */
@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {


    /**
     * Busca los alumnos con una inscripción activa a alguna cohorte activa del curso, sin repetirlos.
     *
     * @param curso el curso
     * @return los alumnos inscriptos al curso
     */
    @Query("SELECT DISTINCT a FROM Alumno a JOIN a.inscripciones i WHERE i.baja = false AND i.cohorte.baja = false "
            + "AND i.cohorte.curso = :curso")
    List<Alumno> buscarPorCurso(@Param("curso") Curso curso);

    /**
     * Busca los alumnos con una inscripción activa a la cohorte.
     *
     * @param cohorte la cohorte
     * @return los alumnos inscriptos a la cohorte
     */
    @Query("SELECT DISTINCT a FROM Alumno a JOIN a.inscripciones i WHERE i.baja = false AND i.cohorte = :cohorte")
    List<Alumno> buscarPorCohorte(@Param("cohorte") Cohorte cohorte);
}
