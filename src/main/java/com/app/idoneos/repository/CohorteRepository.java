package com.app.idoneos.repository;

import java.util.Collection;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Cohorte} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Cohorte}.
 */
@Repository
public interface CohorteRepository extends JpaRepository<Cohorte, Integer> {

    /**
     * Todas las cohortes con su curso, programa y estado ya cargados (en una sola consulta, no una por cohorte).
     *
     * @return Todas las cohortes.
     */
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "curso", "programa", "estadoCohorte" })
    List<Cohorte> findAll();

    /**
     * Las cohortes de los cursos indicados, con su curso, programa y estado ya cargados (para el docente, que solo ve las de
     * sus cursos).
     *
     * @param cursos Los cursos.
     * @return Las cohortes de esos cursos.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "curso", "programa", "estadoCohorte" })
    List<Cohorte> findByCursoIn(java.util.Collection<Curso> cursos);

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Cohorte} activos.
     */
    List<Cohorte> findByBajaFalse();

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    List<Cohorte> findByProgramaAndBajaFalse(Programa programa);

    /**
     * Busca las cohortes vigentes del curso, tengan o no programa.
     *
     * @param curso el curso
     * @return las cohortes activas del curso
     */
    List<Cohorte> findByCursoAndBajaFalse(Curso curso);
}
