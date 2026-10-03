package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Modalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link CursoModalidad} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link CursoModalidad}.
 */
@Repository
public interface CursoModalidadRepositorio extends JpaRepository<CursoModalidad, Integer> {

    /**
     * Busca los registros de cursoModalidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de CursoModalidad asociados al Curso indicado
     */
    List<CursoModalidad> findByCurso(Curso curso);

    /**
     * Busca los registros de cursoModalidad asociados a modalidad.
     *
     * @param modalidad el registro de Modalidad asociado
     * @return una lista de CursoModalidad asociados al Modalidad indicado
     */
    List<CursoModalidad> findByModalidad(Modalidad modalidad);

    /**
     * Busca la relación entre curso y modalidad a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param curso el registro de Curso asociado
     * @param modalidad el registro de Modalidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<CursoModalidad> findByCursoAndModalidad(Curso curso, Modalidad modalidad);
}
