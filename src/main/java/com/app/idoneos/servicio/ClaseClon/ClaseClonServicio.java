package com.app.idoneos.servicio.ClaseClon;

import java.util.List;
import com.app.idoneos.modelo.ClaseClon;
import com.app.idoneos.modelo.EstadoClaseClon;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;

/**
 * Servicio para gestionar las operaciones relacionadas con la clase con clon.
 */
public interface ClaseClonServicio {

    /**
     * Busca los registros vigentes de claseClon asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de ClaseClon asociados al ParticipacionDocente indicado
     */
    List<ClaseClon> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente);

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
