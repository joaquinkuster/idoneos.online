package com.app.idoneos.service.estadoclaseenvivo;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.EstadoClaseEnVivo;
import java.util.Optional;

/** Servicio del estado de la clase en vivo. */
public interface EstadoClaseEnVivoService extends CrudService<EstadoClaseEnVivo> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el estado de la clase en vivo por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoClaseEnVivo> buscarPorNombre(String nombre);
}
