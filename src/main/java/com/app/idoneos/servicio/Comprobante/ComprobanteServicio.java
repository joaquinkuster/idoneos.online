package com.app.idoneos.servicio.Comprobante;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Pago;

/**
 * Servicio para gestionar las operaciones relacionadas con el comprobante.
 */
public interface ComprobanteServicio {

    /**
     * Busca los registros de comprobante asociados a pago.
     *
     * @param pago el registro de Pago asociado
     * @return una lista de Comprobante asociados al Pago indicado
     */
    List<Comprobante> buscarPorPago(Pago pago);

    /**
     * Busca el comprobante por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Comprobante> buscarPorNumero(String numero);
}
