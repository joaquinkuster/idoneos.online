package com.app.idoneos.servicio.EstadoClaseClon;

import java.util.Optional;
import com.app.idoneos.modelo.EstadoClaseClon;

/**
 * Servicio para gestionar las operaciones relacionadas con el estado de la clase con clon.
 */
public interface EstadoClaseClonServicio {

    /**
     * Busca el estado de la clase con clon por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoClaseClon> buscarPorNombre(String nombre);
}
