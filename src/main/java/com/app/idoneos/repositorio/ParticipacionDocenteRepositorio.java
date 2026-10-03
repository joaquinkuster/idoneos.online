package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link ParticipacionDocente} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link ParticipacionDocente}.
 */
@Repository
public interface ParticipacionDocenteRepositorio extends JpaRepository<ParticipacionDocente, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link ParticipacionDocente} activos.
     */
    List<ParticipacionDocente> findByBajaFalse();

    /**
     * Busca los registros vigentes de participacionDocente asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de ParticipacionDocente asociados al Curso indicado
     */
    List<ParticipacionDocente> findByCursoAndBajaFalse(Curso curso);

    /**
     * Busca los registros vigentes de participacionDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de ParticipacionDocente asociados al Docente indicado
     */
    List<ParticipacionDocente> findByDocenteAndBajaFalse(Docente docente);

    /**
     * Busca los registros vigentes de participacionDocente asociados a programa.
     *
     * @param programaPorDefecto el registro de Programa asociado
     * @return una lista de ParticipacionDocente asociados al Programa indicado
     */
    List<ParticipacionDocente> findByProgramaPorDefectoAndBajaFalse(Programa programaPorDefecto);

    /**
     * Busca los registros vigentes de participacionDocente asociados a cohorte.
     *
     * @param cohortePorDefecto el registro de Cohorte asociado
     * @return una lista de ParticipacionDocente asociados al Cohorte indicado
     */
    List<ParticipacionDocente> findByCohortePorDefectoAndBajaFalse(Cohorte cohortePorDefecto);
}
