package com.app.idoneos.service.comprobante;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Comprobante;
import com.app.idoneos.model.Pago;
import java.util.Optional;

/** Servicio del comprobante. */
public interface ComprobanteService extends CrudService<Comprobante> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el comprobante por su atributo único 'numero'.
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

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Emite el comprobante de un pago aprobado: le asigna su número, lo guarda y lo agrega al pago.
     *
     * @param pago El pago aprobado.
     * @param nombrePagador El nombre del pagador, para el comprobante.
     * @param dniPagador El DNI del pagador, para el comprobante.
     * @return El comprobante emitido.
     */
    Comprobante emitir(Pago pago, String nombrePagador, String dniPagador);
}
