package com.app.idoneos.servicio.EstadoClaseEnVivo;

import java.util.Optional;
import com.app.idoneos.modelo.EstadoClaseEnVivo;

/**
 * Servicio para gestionar las operaciones relacionadas con el estado de la clase en vivo.
 */
public interface EstadoClaseEnVivoServicio {

    /**
     * Busca el estado de la clase en vivo por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoClaseEnVivo> buscarPorNombre(String nombre);
}
