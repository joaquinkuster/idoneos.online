package com.app.idoneos.servicio.TipoMaterial;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.TipoMaterial;

/**
 * Servicio para gestionar las operaciones relacionadas con el tipo de material.
 */
public interface TipoMaterialServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el tipo de material por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TipoMaterial> buscarPorNombre(String nombre);

    /**
     * Obtiene los tipos de material que el docente puede subir manualmente (Grabación, Bibliografía y
     * Presentación), en el orden en que se ofrecen.
     *
     * @return los tipos de material que se suben manualmente
     */
    List<TipoMaterial> buscarSubidosManualmente();
}
