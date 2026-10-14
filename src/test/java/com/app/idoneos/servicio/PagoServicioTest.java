package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.repositorio.DescuentoRepositorio;
import com.app.idoneos.repositorio.EstadoPagoRepositorio;
import com.app.idoneos.repositorio.MetodoPagoRepositorio;
import com.app.idoneos.repositorio.PagoRepositorio;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;
import com.app.idoneos.servicio.Pago.PagoServicioImpl;
import com.app.idoneos.servicio.Pago.PasarelaPago;
import com.app.idoneos.servicio.Pago.PasarelaSimulada;
import com.app.idoneos.servicio.Pago.SolicitudDePago;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.ComprobanteUtilidad;

/**
 * Pruebas de las reglas de negocio de los pagos (CU-50 a CU-54), con la plataforma de pagos simulada y sobre los
 * datos de la semilla. Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 *
 * En la semilla, Lucía tiene su inscripción a Finanzas Personales ($ 80.000) pendiente de pago, con un pago de MODO
 * rechazado por fondos insuficientes; el resto de las inscripciones con costo tienen su pago aprobado.
 */
@SpringBootTest
@Transactional
class PagoServicioTest {

    private static final String LUCIA = "lucia.fernandez@correo.com";
    private static final String MARTIN = "martin.gomez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private PagoServicioImpl pagoServicio;

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    @Autowired
    private PasarelaSimulada pasarela;

    @Autowired
    private PagoRepositorio pagoRepositorio;

    @Autowired
    private DescuentoRepositorio descuentoRepositorio;

    @Autowired
    private EstadoPagoRepositorio estadoPagoRepositorio;

    @Autowired
    private MetodoPagoRepositorio metodoPagoRepositorio;

    @Autowired
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @BeforeEach
    @AfterEach
    void reiniciarPasarela() {
        pasarela.reiniciar();
    }

    private Inscripcion pendienteDeLucia() {
        return inscripcionServicio.buscarConFiltros(null, LUCIA, Inscripcion.ACCESO_PENDIENTE_DE_PAGO).get(0);
    }

    private Alumno alumno(String correo) {
        return inscripcionServicio.buscarConFiltros(null, correo, null).get(0).getAlumno();
    }

    /** Inscribe a un alumno a Finanzas Personales ($ 80.000), que queda pendiente de pago. */
    private Inscripcion inscribirAFinanzas(String correo) {
        Integer idPrograma = programaServicio.buscarPorCurso(cursoServicio.buscarPorNombre("Finanzas Personales e Inversión")
                .orElseThrow()).get(0).getIdPrograma();
        Integer idCohorte = cohorteServicio.buscarConFiltros(idPrograma, null, "Abierta", null, null, "recientes", null)
                .get(0).getIdCohorte();
        return inscripcionServicio.inscribir(alumno(correo), idCohorte, true);
    }

    private Descuento descuento(String nombre) {
        return descuentoRepositorio.findByNombre(nombre).orElseThrow();
    }

    private PasarelaPago.Resultado rechazado(String motivo) {
        return new PasarelaPago.Resultado(PasarelaPago.Estado.RECHAZADO, null, "Tarjeta de crédito Visa", "0008", null, null,
                null, motivo, null);
    }

    // ---------- Descuento que le corresponde al pagar ----------

    @Test
    @DisplayName("CU-51: el descuento sin condición se aplica a todos y se calcula el monto con él")
    void descuentoSinCondicion() {
        Inscripcion lucia = pendienteDeLucia();

        // Lucía compró 1 curso (Mercado de Capitales): le corresponde el de bienvenida (10%), no el de 2 cursos
        assertEquals("Descuento de bienvenida", pagoServicio.calcularDescuento(lucia).orElseThrow().getNombre());
        assertEquals(72000f, pagoServicio.calcularMontoAPagar(lucia));
    }

    @Test
    @DisplayName("CU-51: aplica un solo descuento, el de mayor porcentaje entre los que cumple")
    void descuentoDeMayorPorcentaje() {
        // Valentina compró 2 cursos (Mercado y Análisis): cumple "Alumno frecuente" (20%) y el de bienvenida (10%)
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);

        assertEquals("Alumno frecuente", pagoServicio.calcularDescuento(nueva).orElseThrow().getNombre());
        assertEquals(64000f, pagoServicio.calcularMontoAPagar(nueva));
    }

    @Test
    @DisplayName("CU-51: no aplica descuentos vencidos, agotados, próximos ni dados de baja")
    void descuentosNoVigentes() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);
        descuento("Alumno frecuente").setCantidadUsada(50); // agotado
        assertEquals("Descuento de bienvenida", pagoServicio.calcularDescuento(nueva).orElseThrow().getNombre());

        descuento("Descuento de bienvenida").marcarInactivo();
        assertTrue(pagoServicio.calcularDescuento(nueva).isEmpty());
        assertEquals(80000f, pagoServicio.calcularMontoAPagar(nueva));
    }

    @Test
    @DisplayName("CU-51: los cursos comprados son las inscripciones habilitadas con pago aprobado")
    void cursosComprados() {
        // Valentina compró 2 cursos y Lucía solo 1: el descuento de 2 cursos le corresponde a Valentina y no a Lucía
        Inscripcion deValentina = inscribirAFinanzas(VALENTINA);
        assertEquals("Alumno frecuente", pagoServicio.calcularDescuento(deValentina).orElseThrow().getNombre());
        assertNotEquals("Alumno frecuente", pagoServicio.calcularDescuento(pendienteDeLucia()).orElseThrow().getNombre());

        // Si el descuento exige 3 cursos comprados, Valentina (que compró 2) deja de cumplirlo
        descuento("Alumno frecuente").setCursosRequeridos(3);
        assertNotEquals("Alumno frecuente", pagoServicio.calcularDescuento(deValentina).orElseThrow().getNombre());
    }

    // ---------- CU-51 Realizar pago / CU-52 Reintentar pago ----------

    @Test
    @DisplayName("CU-52: reintentar reutiliza el registro del pago rechazado, con un identificador de intención nuevo")
    void reintentaReutilizandoElRegistro() {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();
        assertTrue(rechazado.estaRechazado());
        String idExternoAnterior = rechazado.getIdIntencionExterna();

        SolicitudDePago solicitud = pagoServicio.reintentarPago(rechazado.getIdPago(), lucia.getAlumno());

        assertEquals(rechazado.getIdPago(), solicitud.idPago());
        assertTrue(solicitud.simulado());
        assertFalse(solicitud.aprobado());
        assertTrue(rechazado.estaPendiente());
        assertNotEquals(idExternoAnterior, rechazado.getIdIntencionExterna());
        assertEquals(solicitud.idSolicitud(), rechazado.getIdSolicitudPago());
        assertEquals(72000f, rechazado.getMonto());
        assertEquals("Descuento de bienvenida", rechazado.getDescuento().getNombre());
        assertNull(rechazado.getDetalleErrorOperacion());
        assertEquals(1, lucia.getPagos().size()); // el mismo registro, no uno nuevo
    }

    @Test
    @DisplayName("CU-51: un pago nuevo se registra pendiente, con MODO como método, el monto con descuento y su solicitud")
    void iniciaUnPagoNuevo() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);

        SolicitudDePago solicitud = pagoServicio.iniciarPago(nueva.getIdInscripcion(), nueva.getAlumno());

        Pago pago = pagoRepositorio.findById(solicitud.idPago()).orElseThrow();
        assertTrue(pago.estaPendiente());
        assertEquals(MetodoPago.MODO, pago.getMetodoPago().getNombre());
        assertEquals(64000f, pago.getMonto());
        assertTrue(pago.getIdIntencionExterna().startsWith("IDN-"));
        assertTrue(solicitud.idSolicitud().startsWith("SIM-"));
        assertNotNull(solicitud.qr());
        assertEquals(0, descuento("Alumno frecuente").getCantidadUsada()); // el uso se cuenta al aprobarse el pago
        assertFalse(nueva.estaHabilitada()); // el acceso sigue sin habilitar hasta que se apruebe
    }

    @Test
    @DisplayName("CU-51: con el pago aprobado se habilita el acceso, se cuenta el uso del descuento y se emite el comprobante")
    void apruebaElPago() {
        Inscripcion lucia = pendienteDeLucia();
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());

        Pago pago = pagoServicio.consultarEstado(solicitud.idPago(), lucia.getAlumno()); // la plataforma aprueba

        assertTrue(pago.estaAprobado());
        assertNotNull(pago.getFechaAprobacion());
        assertEquals("Tarjeta de crédito Visa", pago.getDetalleMetodoPago());
        assertEquals("0008", pago.getUltimosDigitosTarjeta());
        assertNotNull(pago.getCodigoReferencia());
        assertTrue(lucia.estaHabilitada());
        assertEquals(Inscripcion.ACCESO_VIGENTE, lucia.getEstadoAcceso());
        assertEquals(usosAntes + 1, descuento("Descuento de bienvenida").getCantidadUsada());
        assertNotNull(pago.getComprobanteVigente());
        assertTrue(pago.getComprobanteVigente().getNumero().startsWith("COMP-" + LocalDate.now().getYear() + "-"));
        assertEquals("Pagador Simulado", pago.getComprobanteVigente().getNombrePagador());
        assertEquals("30111222", pago.getComprobanteVigente().getDniPagador());
        assertFalse(pago.getComprobanteVigente().getEnviado()); // el envío por correo es de MOD-NF-01
    }

    @Test
    @DisplayName("CU-51: un pago rechazado queda rechazado con el motivo, no habilita el acceso ni cuenta el descuento")
    void rechazaElPago() {
        Inscripcion lucia = pendienteDeLucia();
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());
        pasarela.resolver(solicitud.idSolicitud(), rechazado("INSUFFICIENT_FUNDS"));

        Pago pago = pagoServicio.consultarEstado(solicitud.idPago(), lucia.getAlumno());

        assertTrue(pago.estaRechazado());
        assertEquals("INSUFFICIENT_FUNDS", pago.getDetalleErrorOperacion());
        assertFalse(lucia.estaHabilitada());
        assertEquals(usosAntes, descuento("Descuento de bienvenida").getCantidadUsada());
        assertNull(pago.getComprobanteVigente());
        // y se puede reintentar de nuevo con el mismo registro
        assertEquals(solicitud.idPago(), pagoServicio.reintentarPago(pago.getIdPago(), lucia.getAlumno()).idPago());
    }

    @Test
    @DisplayName("CU-51: los montos de rechazo de MODO simulan sus errores")
    void montosDeRechazoDeModo() {
        PasarelaPago.Solicitud solicitud = pasarela.crearSolicitud("IDN-1", 25.64f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222");
        PasarelaPago.Resultado resultado = pasarela.consultarPago(solicitud.id());

        assertEquals(PasarelaPago.Estado.RECHAZADO, resultado.estado());
        assertEquals("INSUFFICIENT_FUNDS", resultado.motivoError());
    }

    @Test
    @DisplayName("CU-51: sin confirmación el pago sigue pendiente y, vencido el plazo, se rechaza")
    void vencePorFaltaDeConfirmacion() {
        pasarela.setEsperarConfirmacion(true);
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepositorio.findById(solicitud.idPago()).orElseThrow();

        assertTrue(pagoServicio.consultarEstado(pago.getIdPago(), lucia.getAlumno()).estaPendiente());

        pago.setUltimaModificacion(LocalDateTime.now().minusMinutes(pagoServicio.plazoConfirmacionMinutos() + 1));
        pago = pagoServicio.consultarEstado(pago.getIdPago(), lucia.getAlumno());

        assertTrue(pago.estaRechazado());
        assertTrue(pago.getDetalleErrorOperacion().contains("no se confirmó dentro del plazo"));
        assertFalse(lucia.estaHabilitada());
    }

    @Test
    @DisplayName("CU-51: si MODO ya aprobó el pago, vence el plazo pero gana el resultado de MODO")
    void elResultadoDeModoGanaAlPlazo() {
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepositorio.findById(solicitud.idPago()).orElseThrow();
        pago.setUltimaModificacion(LocalDateTime.now().minusMinutes(30));

        assertTrue(pagoServicio.consultarEstado(pago.getIdPago(), lucia.getAlumno()).estaAprobado());
    }

    @Test
    @DisplayName("CU-51: volver a pagar con un pago en curso retoma su solicitud en lugar de crear otra")
    void retomaElPagoEnCurso() {
        pasarela.setEsperarConfirmacion(true);
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago primera = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());

        SolicitudDePago segunda = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());

        assertEquals(primera.idPago(), segunda.idPago());
        assertEquals(primera.idSolicitud(), segunda.idSolicitud());
        assertNotNull(segunda.qr());
        assertEquals(1, lucia.getPagos().size());
    }

    @Test
    @DisplayName("CU-51: si el descuento cubre todo el precio, el pago se aprueba sin pasar por MODO")
    void descuentoTotal() {
        descuentoRepositorio.save(new Descuento("Beca completa", 0, 100, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), 5, 0));
        Inscripcion lucia = pendienteDeLucia();

        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());

        assertTrue(solicitud.aprobado());
        Pago pago = pagoRepositorio.findById(solicitud.idPago()).orElseThrow();
        assertTrue(pago.estaAprobado());
        assertEquals(0f, pago.getMonto());
        assertEquals(MetodoPago.SIN_COSTO, pago.getMetodoPago().getNombre());
        assertTrue(lucia.estaHabilitada());
        assertEquals(1, descuento("Beca completa").getCantidadUsada());
        assertNotNull(pago.getComprobanteVigente());
    }

    @Test
    @DisplayName("CU-51: no se crea una solicitud por un monto menor al mínimo de MODO")
    void montoMenorAlMinimo() {
        Inscripcion lucia = pendienteDeLucia();
        lucia.getCurso().setPrecio(20f);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno()));
        assertTrue(error.getMessage().contains("mínimo"));
    }

    @Test
    @DisplayName("CU-51: solo el dueño paga una inscripción que no esté habilitada, paga ni vencida")
    void validacionesDelPago() {
        Inscripcion lucia = pendienteDeLucia();
        assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.iniciarPago(lucia.getIdInscripcion(), alumno(MARTIN)));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.iniciarPago(lucia.getIdInscripcion(), null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.iniciarPago(99999, lucia.getAlumno()));

        Inscripcion deMartin = inscripcionServicio.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0);
        IllegalArgumentException yaPaga = assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.iniciarPago(deMartin.getIdInscripcion(), deMartin.getAlumno()));
        assertTrue(yaPaga.getMessage().contains("ya está aprobado"));

        lucia.setFechaVencimientoAcceso(LocalDateTime.now().minusDays(1));
        assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno()));
    }

    @Test
    @DisplayName("CU-52: solo se reintenta un pago rechazado, de una inscripción sin acceso, y propio")
    void validacionesDelReintento() {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.reintentarPago(rechazado.getIdPago(), alumno(MARTIN)));

        pagoServicio.reintentarPago(rechazado.getIdPago(), lucia.getAlumno()); // ahora está pendiente
        IllegalArgumentException pendiente = assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.reintentarPago(rechazado.getIdPago(), lucia.getAlumno()));
        assertTrue(pendiente.getMessage().contains("no se encuentra rechazado"));
    }

    @Test
    @DisplayName("CU-51: el aviso de MODO identifica el pago por su solicitud o su identificador externo y se concilia")
    void procesaElAvisoDeModo() {
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepositorio.findById(solicitud.idPago()).orElseThrow();

        assertFalse(pagoServicio.procesarNotificacion("SIM-desconocida", null));
        assertFalse(pagoServicio.procesarNotificacion(null, null));
        assertTrue(pagoServicio.procesarNotificacion(null, pago.getIdIntencionExterna()));

        assertTrue(pago.estaAprobado());
    }

    @Test
    @DisplayName("CU-51: la conciliación periódica resuelve los pagos pendientes")
    void conciliaLosPendientes() {
        pasarela.setEsperarConfirmacion(true);
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());
        assertEquals(0, pagoServicio.conciliarPagosPendientes());

        pasarela.setEsperarConfirmacion(false);
        assertEquals(1, pagoServicio.conciliarPagosPendientes());
        assertTrue(pagoRepositorio.findById(solicitud.idPago()).orElseThrow().estaAprobado());
    }

    // ---------- CU-53 Registrar pago ----------

    @Test
    @DisplayName("CU-53: registra el pago por transferencia: queda aprobado, habilita el acceso y emite el comprobante")
    void registraUnPagoManual() {
        Inscripcion lucia = pendienteDeLucia();
        Pago anterior = lucia.getPagoSinAprobar(); // el rechazado de MODO se reutiliza

        Pago pago = pagoServicio.registrarPagoManual(lucia.getIdInscripcion(), 72000f, " TRF-778899 ", "Transferencia del 12/10");

        assertEquals(anterior.getIdPago(), pago.getIdPago());
        assertTrue(pago.estaAprobado());
        assertEquals(MetodoPago.TRANSFERENCIA, pago.getMetodoPago().getNombre());
        assertEquals("TRF-778899", pago.getCodigoReferencia());
        assertTrue(pago.getDetalleMetodoPago().contains("Transferencia del 12/10"));
        assertNull(pago.getDescuento());
        assertNull(pago.getDetalleErrorOperacion());
        assertTrue(lucia.estaHabilitada());
        assertEquals("Lucía Fernández", pago.getComprobanteVigente().getNombrePagador());
        assertEquals("40123456", pago.getComprobanteVigente().getDniPagador());
    }

    @Test
    @DisplayName("CU-53: informa los datos obligatorios que faltan y los inválidos")
    void validacionesDelPagoManual() {
        Inscripcion lucia = pendienteDeLucia();
        Integer id = lucia.getIdInscripcion();

        assertThrows(IllegalArgumentException.class, () -> pagoServicio.registrarPagoManual(id, null, "TRF-1", null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.registrarPagoManual(id, 100f, " ", null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.registrarPagoManual(id, 0f, "TRF-1", null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.registrarPagoManual(id, 100f, "X".repeat(21), null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.registrarPagoManual(id, 100f, "TRF-1", "x".repeat(301)));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.registrarPagoManual(99999, 100f, "TRF-1", null));
        IllegalArgumentException repetido = assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.registrarPagoManual(id, 100f, "TRF-000451", null)); // el de la semilla
        assertTrue(repetido.getMessage().contains("Ya existe un pago"));
        assertFalse(lucia.estaHabilitada());
    }

    @Test
    @DisplayName("CU-53: no se registra el pago de una inscripción que ya está paga")
    void pagoManualDeUnaInscripcionPaga() {
        Inscripcion deMartin = inscripcionServicio.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0);

        assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.registrarPagoManual(deMartin.getIdInscripcion(), 100f, "TRF-NUEVO", null));
    }

    @Test
    @DisplayName("CU-53: una inscripción nueva sin pagos recibe un registro de pago nuevo")
    void pagoManualDeUnaInscripcionNueva() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);

        Pago pago = pagoServicio.registrarPagoManual(nueva.getIdInscripcion(), 80000f, "TRF-900001", null);

        assertTrue(pago.estaAprobado());
        assertTrue(nueva.estaHabilitada());
        assertEquals(1, nueva.getPagos().size());
    }

    // ---------- CU-54 Modificar pago ----------

    @Test
    @DisplayName("CU-54: acredita un pago de MODO rechazado indicando el pagador: habilita el acceso y emite el comprobante")
    void acreditaUnPagoDeModo() {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();

        Pago pago = pagoServicio.modificarPago(rechazado.getIdPago(), "Aprobado", 72000f, null, "Lucía Fernández", "40123456", null);

        assertTrue(pago.estaAprobado());
        assertEquals(72000f, pago.getMonto());
        assertTrue(pago.getDetalleMetodoPago().contains("Acreditado manualmente"));
        assertTrue(lucia.estaHabilitada());
        assertEquals("Lucía Fernández", pago.getComprobanteVigente().getNombrePagador());
        assertEquals(MetodoPago.MODO, pago.getMetodoPago().getNombre());
    }

    @Test
    @DisplayName("CU-54: al aprobar un pago con descuento cuenta su uso, igual que el pago online")
    void acreditarCuentaElDescuento() {
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());
        pasarela.resolver(solicitud.idSolicitud(), rechazado("EXPIRED_CARD"));
        pagoServicio.consultarEstado(solicitud.idPago(), lucia.getAlumno());
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();

        pagoServicio.modificarPago(solicitud.idPago(), "Aprobado", 72000f, null, "Lucía Fernández", "40123456", null);

        assertEquals(usosAntes + 1, descuento("Descuento de bienvenida").getCantidadUsada());
    }

    @Test
    @DisplayName("CU-54: exige el nombre y el DNI del pagador para aprobar un pago de MODO")
    void exigeElPagadorDeModo() {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        Integer id = rechazado.getIdPago();

        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(id, "Aprobado", 72000f, null, "", "40123456", null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(id, "Aprobado", 72000f, null, "Lucía", "abc", null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(id, "Aprobado", 72000f, null, "Lucía", null, null));
        assertTrue(rechazado.estaRechazado());
    }

    @Test
    @DisplayName("CU-54: marca un pago pendiente como rechazado, con el motivo")
    void rechazaManualmente() {
        pasarela.setEsperarConfirmacion(true);
        Inscripcion lucia = pendienteDeLucia();
        SolicitudDePago solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());

        Pago pago = pagoServicio.modificarPago(solicitud.idPago(), "Rechazado", 72000f, null, null, null, "No se acreditó");

        assertTrue(pago.estaRechazado());
        assertEquals("No se acreditó", pago.getDetalleErrorOperacion());
        assertFalse(lucia.estaHabilitada());
        assertEquals("Rechazado por el administrador.",
                pagoServicio.modificarPago(solicitud.idPago(), "Rechazado", 72000f, null, null, null, " ").getDetalleErrorOperacion());
    }

    @Test
    @DisplayName("CU-54: solo se modifica un pago pendiente o rechazado, con estado y monto válidos")
    void validacionesDeModificarPago() {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        Pago aprobado = inscripcionServicio.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0).getPagos().iterator().next();

        IllegalArgumentException estado = assertThrows(IllegalArgumentException.class,
                () -> pagoServicio.modificarPago(aprobado.getIdPago(), "Rechazado", 100f, "TRF-1", null, null, null));
        assertTrue(estado.getMessage().contains("pendiente o rechazado"));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(rechazado.getIdPago(), "Pendiente", 100f, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(rechazado.getIdPago(), null, 100f, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(rechazado.getIdPago(), "Rechazado", null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(rechazado.getIdPago(), "Rechazado", -5f, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(99999, "Aprobado", 100f, null, null, null, null));
    }

    @Test
    @DisplayName("CU-54: un pago por transferencia exige su comprobante y no admite uno repetido")
    void transferenciaExigeComprobante() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);
        Pago transferencia = pagoRepositorio.save(new Pago(estadoPagoRepositorio.findByNombre(EstadoPago.PENDIENTE).orElseThrow(),
                metodoPagoRepositorio.findByNombre(MetodoPago.TRANSFERENCIA).orElseThrow(), nueva, "IDN-PRUEBA-TRF", 80000f));
        Integer id = transferencia.getIdPago();

        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(id, "Aprobado", 80000f, " ", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.modificarPago(id, "Aprobado", 80000f, "TRF-000451", null, null, null));

        Pago pago = pagoServicio.modificarPago(id, "Aprobado", 80000f, "TRF-123456", null, null, null);

        assertTrue(pago.estaAprobado());
        assertEquals("Valentina Ruiz", pago.getComprobanteVigente().getNombrePagador());
    }

    // ---------- CU-50 Buscar pago ----------

    @Test
    @DisplayName("CU-50: busca por estado, curso, alumno y fechas")
    void buscaPagos() {
        assertEquals(6, pagoServicio.buscarConFiltros(null, null, "Aprobado", null, null, null).size());
        assertEquals(1, pagoServicio.buscarConFiltros(null, null, "rechazado", null, null, null).size());
        assertEquals(0, pagoServicio.buscarConFiltros(null, null, "Pendiente", null, null, null).size());
        assertEquals(7, pagoServicio.buscarConFiltros(null, null, null, null, null, null).size());
        assertEquals(3, pagoServicio.buscarConFiltros(null, "martin", null, null, null, null).size());
        assertEquals(1, pagoServicio.buscarConFiltros(null, "40123456", "Rechazado", null, null, null).size());

        Integer idCurso = cursoServicio.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow().getIdCurso();
        assertEquals(3, pagoServicio.buscarConFiltros(idCurso, null, null, null, null, null).size());

        LocalDate hoy = LocalDate.now();
        assertEquals(2, pagoServicio.buscarConFiltros(null, null, null, hoy.minusDays(1), hoy, null).size()); // el de ayer y el de hace 3 horas
        assertEquals(1, pagoServicio.buscarConFiltros(null, null, null, hoy.minusDays(60), hoy.minusDays(40), null).size());
    }

    @Test
    @DisplayName("CU-50: el alumno ve solo sus pagos, del más reciente al más antiguo")
    void elAlumnoVeSoloSusPagos() {
        List<Pago> deLucia = pagoServicio.buscarConFiltros(null, null, null, null, null, alumno(LUCIA));

        assertEquals(2, deLucia.size());
        assertTrue(deLucia.stream().allMatch(p -> p.getInscripcion().getAlumno().getUsuario().getCorreo().equals(LUCIA)));
        assertTrue(deLucia.get(0).estaRechazado()); // el de hace 3 horas va primero
        assertEquals(2, pagoServicio.buscarConFiltros(null, null, null, null, null, alumno(VALENTINA)).size());
    }

    @Test
    @DisplayName("CU-50: el alumno no accede al pago de otro, el administrador sí")
    void pagoVisible() {
        Pago deLucia = pendienteDeLucia().getPagoSinAprobar();

        assertEquals(deLucia, pagoServicio.buscarVisible(deLucia.getIdPago(), alumno(LUCIA)));
        assertEquals(deLucia, pagoServicio.buscarVisible(deLucia.getIdPago(), null));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.buscarVisible(deLucia.getIdPago(), alumno(MARTIN)));
        assertThrows(IllegalArgumentException.class, () -> pagoServicio.buscarVisible(99999, null));
    }

    @Test
    @DisplayName("CU-50: el comprobante de un pago aprobado se genera como PDF")
    void comprobanteEnPdf() {
        Pago aprobado = inscripcionServicio.buscarConFiltros(null, VALENTINA, Inscripcion.ACCESO_VIGENTE).get(0)
                .getPagoAprobado();

        byte[] pdf = ComprobanteUtilidad.generarPdf(aprobado.getComprobanteVigente());

        assertTrue(pdf.length > 800);
        assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    @DisplayName("El plazo de confirmación del pago se toma del parámetro y se limita a lo que admite MODO (5 a 10 minutos)")
    void plazoDeConfirmacion() {
        assertEquals(10, pagoServicio.plazoConfirmacionMinutos());
    }
}
