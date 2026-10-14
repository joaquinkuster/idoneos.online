package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.servicio.Comprobante.ComprobanteServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;

/**
 * Pruebas de la emisión del comprobante de un pago aprobado y de su archivo PDF (CU-50), sobre los datos de la
 * semilla. Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class ComprobanteServicioTest {

    @Autowired
    private ComprobanteServicioImpl comprobanteServicio;

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    /** El pago rechazado de Lucía en la semilla: todavía no tiene comprobante. */
    private Pago pagoSinComprobante() {
        return inscripcionServicio.buscarConFiltros(null, "lucia.fernandez@correo.com", Inscripcion.ACCESO_PENDIENTE_DE_PAGO)
                .get(0).getPagoSinAprobar();
    }

    @Test
    @DisplayName("Emite el comprobante con su número, el pagador, la fecha y sin enviar, y lo agrega al pago")
    void emiteElComprobante() {
        Pago pago = pagoSinComprobante();

        Comprobante comprobante = comprobanteServicio.emitirComprobante(pago, "Lucía Fernández", "40123456");

        assertEquals("COMP-" + LocalDate.now().getYear() + "-" + String.format("%06d", pago.getIdPago()), comprobante.getNumero());
        assertEquals("Lucía Fernández", comprobante.getNombrePagador());
        assertEquals("40123456", comprobante.getDniPagador());
        assertNotNull(comprobante.getFechaEmision());
        assertFalse(comprobante.getEnviado()); // el envío por correo es de MOD-NF-01
        assertEquals(comprobante, pago.getComprobanteVigente());
        assertTrue(comprobanteServicio.buscarPorNumero(comprobante.getNumero()).isPresent());
    }

    @Test
    @DisplayName("Recorta el nombre y el DNI del pagador a los límites de sus columnas")
    void recortaLosDatosDelPagador() {
        Pago pago = pagoSinComprobante();

        Comprobante comprobante = comprobanteServicio.emitirComprobante(pago, "N".repeat(150), "123456789012");

        assertEquals(Comprobante.LONGITUD_NOMBRE_PAGADOR, comprobante.getNombrePagador().length());
        assertEquals(Comprobante.LONGITUD_DNI_PAGADOR, comprobante.getDniPagador().length());
    }

    @Test
    @DisplayName("Si el pagador no tiene datos, el comprobante queda con campos vacíos y no falla")
    void sinDatosDelPagador() {
        Comprobante comprobante = comprobanteServicio.emitirComprobante(pagoSinComprobante(), null, null);

        assertEquals("", comprobante.getNombrePagador());
        assertEquals("", comprobante.getDniPagador());
    }

    @Test
    @DisplayName("El comprobante de un pago aprobado se genera como PDF")
    void generaElPdf() {
        Pago aprobado = inscripcionServicio.buscarConFiltros(null, "valentina.ruiz@correo.com", Inscripcion.ACCESO_VIGENTE)
                .get(0).getPagoAprobado();

        byte[] pdf = comprobanteServicio.generarPdf(aprobado.getComprobanteVigente());

        assertTrue(pdf.length > 800);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    @DisplayName("El PDF incluye la referencia y el descuento cuando el pago los tiene, y se genera igual sin ellos")
    void generaElPdfConYSinDatosOpcionales() {
        Pago pago = pagoSinComprobante();
        Comprobante sinOpcionales = comprobanteServicio.emitirComprobante(pago, "Lucía Fernández", "40123456");
        byte[] corto = comprobanteServicio.generarPdf(sinOpcionales);

        pago.setCodigoReferencia("TRF-777");
        pago.setFechaAprobacion(java.time.LocalDateTime.now());
        byte[] largo = comprobanteServicio.generarPdf(sinOpcionales);

        assertEquals("%PDF", new String(corto, 0, 4, StandardCharsets.US_ASCII));
        assertEquals("%PDF", new String(largo, 0, 4, StandardCharsets.US_ASCII));
        assertTrue(largo.length != corto.length, "con más datos el documento cambia");
    }
}
