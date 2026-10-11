package com.app.idoneos.servicio.Unidad;

import java.util.List;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con la unidad.
 */
public interface UnidadServicio {

    /**
     * Busca los registros vigentes de unidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Unidad asociados al Curso indicado
     */
    List<Unidad> buscarPorCurso(Curso curso);

    /**
     * Indica si un usuario puede ver el contenido de una unidad (glosario, foro): el administrador, el docente que
     * participa en el curso y el alumno con la unidad habilitada.
     *
     * @param unidad  la unidad
     * @param usuario el usuario que quiere verlo
     * @return {@code true} si puede verlo
     */
    boolean puedeVerContenido(Unidad unidad, Usuario usuario);
}
