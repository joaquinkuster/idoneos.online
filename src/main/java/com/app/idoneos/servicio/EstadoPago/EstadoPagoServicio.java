package com.app.idoneos.servicio.EstadoPago;

import java.util.Optional;
import com.app.idoneos.modelo.EstadoPago;

/**
 * Servicio para gestionar las operaciones relacionadas con el estado del pago.
 */
public interface EstadoPagoServicio {

    /**
     * Busca el estado del pago por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoPago> buscarPorNombre(String nombre);
}
