package com.app.idoneos.servicio.Material;

import java.util.List;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con el material.
 */
public interface MaterialServicio {

    /**
     * Busca los registros vigentes de material asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de Material asociados al ParticipacionDocente indicado
     */
    List<Material> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente);

    /**
     * Busca los registros vigentes de material asociados a tipoMaterial.
     *
     * @param tipoMaterial el registro de TipoMaterial asociado
     * @return una lista de Material asociados al TipoMaterial indicado
     */
    List<Material> buscarPorTipoMaterial(TipoMaterial tipoMaterial);

    /**
     * Busca los registros vigentes de material asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Material asociados al Unidad indicado
     */
    List<Material> buscarPorUnidad(Unidad unidad);
}
