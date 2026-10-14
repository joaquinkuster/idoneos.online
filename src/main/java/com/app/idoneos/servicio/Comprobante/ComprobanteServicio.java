package com.app.idoneos.servicio.Comprobante;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Pago;

/**
 * Servicio para gestionar las operaciones relacionadas con el comprobante.
 */
public interface ComprobanteServicio {

    // ---------------------------------------------------------------- Lecturas

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

    /**
     * Genera el archivo PDF de un comprobante, para que el alumno o el administrador lo descarguen.
     *
     * @param comprobante El comprobante emitido por un pago aprobado.
     * @return El contenido del archivo PDF.
     */
    byte[] generarPdf(Comprobante comprobante);

    // ---------------------------------------------------------------- Creaciones

    /**
     * Emite el comprobante de un pago aprobado: le asigna su número, lo guarda y lo agrega al pago. El envío por
     * correo electrónico es parte de MOD-NF-01: por ahora queda registrado en el log y sin enviar.
     *
     * @param pago          El pago aprobado.
     * @param nombrePagador El nombre del pagador, para el comprobante.
     * @param dniPagador    El DNI del pagador, para el comprobante.
     * @return El comprobante emitido.
     */
    Comprobante emitirComprobante(Pago pago, String nombrePagador, String dniPagador);
}
