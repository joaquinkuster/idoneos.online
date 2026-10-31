package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link DocenteCurso} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link DocenteCurso}.
 */
@Repository
public interface DocenteCursoRepositorio extends JpaRepository<DocenteCurso, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link DocenteCurso} activos.
     */
    List<DocenteCurso> findByBajaFalse();

    /**
     * Busca los registros vigentes de docenteCurso asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de DocenteCurso asociados al Curso indicado
     */
    List<DocenteCurso> findByCursoAndBajaFalse(Curso curso);

    /**
     * Busca los registros vigentes de docenteCurso asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de DocenteCurso asociados al Docente indicado
     */
    List<DocenteCurso> findByDocenteAndBajaFalse(Docente docente);

    /**
     * Busca los registros vigentes de docenteCurso asociados a cohorte.
     *
     * @param cohortePorDefecto el registro de Cohorte asociado
     * @return una lista de DocenteCurso asociados al Cohorte indicado
     */
    List<DocenteCurso> findByCohortePorDefectoAndBajaFalse(Cohorte cohortePorDefecto);
}
