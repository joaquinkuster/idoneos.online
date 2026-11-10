package com.app.idoneos.service.estadopago;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.EstadoPago;
import java.util.Optional;

/** Servicio del estado del pago. */
public interface EstadoPagoService extends CrudService<EstadoPago> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el estado del pago por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoPago> buscarPorNombre(String nombre);
}
