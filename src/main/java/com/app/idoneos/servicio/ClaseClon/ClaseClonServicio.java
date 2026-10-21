package com.app.idoneos.servicio.ClaseClon;

import java.util.List;
import com.app.idoneos.modelo.ClaseClon;
import com.app.idoneos.modelo.EstadoClaseClon;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.DocenteCurso;

/**
 * Servicio para gestionar las operaciones relacionadas con la clase con clon.
 */
public interface ClaseClonServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de claseClon asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de ClaseClon asociados al DocenteCurso indicado
     */
    List<ClaseClon> buscarPorDocenteCurso(DocenteCurso docenteCurso);

    /**
     * Busca los registros vigentes de claseClon asociados a estadoClaseClon.
     *
     * @param estadoClaseClon el registro de EstadoClaseClon asociado
     * @return una lista de ClaseClon asociados al EstadoClaseClon indicado
     */
    List<ClaseClon> buscarPorEstadoClaseClon(EstadoClaseClon estadoClaseClon);

    /**
     * Busca los registros vigentes de claseClon asociados a material.
     *
     * @param material el registro de Material asociado
     * @return una lista de ClaseClon asociados al Material indicado
     */
    List<ClaseClon> buscarPorMaterial(Material material);
}
