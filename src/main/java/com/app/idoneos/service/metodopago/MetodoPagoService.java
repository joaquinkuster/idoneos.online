package com.app.idoneos.service.metodopago;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.MetodoPago;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con el método de pago.
 */
public interface MetodoPagoService extends CrudService<MetodoPago> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el método de pago por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<MetodoPago> buscarPorNombre(String nombre);
}
