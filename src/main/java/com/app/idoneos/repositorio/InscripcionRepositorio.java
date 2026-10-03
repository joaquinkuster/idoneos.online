package com.app.idoneos.repositorio;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Categoria;
import java.util.List;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Inscripcion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Inscripcion}.
 */
@Repository
public interface InscripcionRepositorio extends JpaRepository<Inscripcion, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Inscripcion} activos.
     */
    List<Inscripcion> findByBajaFalse();

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    List<Inscripcion> findByCohorteAndBajaFalse(Cohorte cohorte);

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    List<Inscripcion> findByAlumnoAndBajaFalse(Alumno alumno);

    /**
     * Cuenta las inscripciones activas (no dadas de baja) asociadas a un curso, a través de su cohorte y programa.
     *
     * @param curso El curso a consultar.
     * @return La cantidad de inscripciones activas del curso.
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.cohorte.programa.curso = :curso")
    long contarActivasPorCurso(@Param("curso") Curso curso);

    /**
     * Cuenta las inscripciones activas (no dadas de baja) asociadas a una categoría, a través de los cursos de la categoría.
     *
     * @param categoria La categoría a consultar.
     * @return La cantidad de inscripciones activas de la categoría.
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.cohorte.programa.curso.categoria = :categoria")
    long contarActivasPorCategoria(@Param("categoria") Categoria categoria);
}
