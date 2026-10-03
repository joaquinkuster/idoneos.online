package com.app.idoneos.servicio.Descuento;

import java.util.Optional;
import com.app.idoneos.modelo.Descuento;

/**
 * Servicio para gestionar las operaciones relacionadas con el descuento.
 */
public interface DescuentoServicio {

    /**
     * Busca el descuento por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Descuento> buscarPorNombre(String nombre);
}
