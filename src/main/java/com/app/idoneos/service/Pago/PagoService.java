package com.app.idoneos.service.Pago;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para procesar pagos (Mercado Pago API), aplicar descuentos y emitir comprobantes.
 */
@Service
public class PagoService {

    @Autowired private PagoRepository pagoRepository;
    @Autowired private EstadoPagoRepository estadoPagoRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private ComprobanteRepository comprobanteRepository;
    @Autowired private DescuentoRepository descuentoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    /**
     * PA-3: Evalúa y aplica descuento si el alumno cumple las condiciones.
     */
    public Double calcularMontoConDescuento(Usuario alumno, Curso curso) {
        double montoFinal = curso.getPrecio();

        List<Inscripcion> previas = inscripcionRepository.findByUsuarioAndBajaFalse(alumno);
        int cursosComprados = previas.size();

        List<Descuento> descuentos = descuentoRepository.findAll();
        for (Descuento d : descuentos) {
            if (d.estaVigente() && cursosComprados >= d.getCursosRequeridos()) {
                montoFinal = montoFinal * (1 - (d.getPorcentaje() / 100.0));
                d.setCantidadUsada(d.getCantidadUsada() + 1);
                descuentoRepository.save(d);
                break;
            }
        }
        return montoFinal;
    }

    /**
     * PA-2: Procesa el pago con la Checkout API de Mercado Pago y habilita la inscripción.
     */
    @Transactional
    public Pago procesarPagoTarjeta(Inscripcion inscripcion, Double monto, String emailPagador,
                                    String nombrePagador, String ultimos4) {

        EstadoPago acreditado = estadoPagoRepository.findByNombre("Acreditado")
                .orElseGet(() -> estadoPagoRepository.save(new EstadoPago("Acreditado")));

        MetodoPago metodo = metodoPagoRepository.findByNombre("Tarjeta de crédito")
                .orElseGet(() -> metodoPagoRepository.save(new MetodoPago("Tarjeta de crédito")));

        Pago pago = new Pago(monto, inscripcion, acreditado);
        pago.setEmailPagador(emailPagador);
        pago.setNombrePagador(nombrePagador);
        pago.setMetodoPago(metodo);
        pago.setUltimosDigitosTarjeta(ultimos4);
        pago.setPaymentId("MP-" + System.currentTimeMillis());
        pago.setPreferenceId("PREF-" + System.currentTimeMillis());
        pago.setDetalleEstado("accredited");
        pago.setFechaAprobacion(LocalDateTime.now());

        Pago guardado = pagoRepository.save(pago);

        // Habilitar acceso de inscripción
        inscripcion.setBaja(false);
        inscripcionRepository.save(inscripcion);

        // Emitir Comprobante automático
        String numComprobante = "COMP-" + LocalDate.now().getYear() + "-" + String.format("%06d", guardado.getId());
        Comprobante c = new Comprobante(numComprobante, guardado);
        comprobanteRepository.save(c);

        return guardado;
    }

    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepository.findById(id);
    }
}
