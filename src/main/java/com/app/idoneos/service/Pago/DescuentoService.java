package com.app.idoneos.service.Pago;

import com.app.idoneos.model.Descuento;
import com.app.idoneos.service.CrudService;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de cupones y promociones de descuento (CU-47 a CU-50).
 */
public interface DescuentoService extends CrudService<Descuento> {

    Optional<Descuento> buscarPorId(Integer id);

    Optional<Descuento> buscarPorCodigo(String codigo);

    List<Descuento> obtenerDescuentosVigentes();

    Descuento registrarDescuento(Descuento descuento);

    Descuento modificarDescuento(Descuento descuento);

    void darDeBajaDescuento(int descuentoId);
}
