package com.app.idoneos.servicio.CursoModalidad;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Modalidad;

/**
 * Servicio para gestionar las operaciones relacionadas con la relación entre curso y modalidad.
 */
public interface CursoModalidadServicio {

    /**
     * Busca los registros de cursoModalidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de CursoModalidad asociados al Curso indicado
     */
    List<CursoModalidad> buscarPorCurso(Curso curso);

    /**
     * Busca los registros de cursoModalidad asociados a modalidad.
     *
     * @param modalidad el registro de Modalidad asociado
     * @return una lista de CursoModalidad asociados al Modalidad indicado
     */
    List<CursoModalidad> buscarPorModalidad(Modalidad modalidad);

    /**
     * Busca la relación entre curso y modalidad a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param curso el registro de Curso asociado
     * @param modalidad el registro de Modalidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<CursoModalidad> buscarPorCursoYModalidad(Curso curso, Modalidad modalidad);
}
