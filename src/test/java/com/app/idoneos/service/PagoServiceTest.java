package com.app.idoneos.service;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.EstadoPago;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.MetodoPago;
import com.app.idoneos.model.Pago;
import com.app.idoneos.repository.DescuentoRepository;
import com.app.idoneos.repository.EstadoPagoRepository;
import com.app.idoneos.repository.MetodoPagoRepository;
import com.app.idoneos.repository.PagoRepository;
import com.app.idoneos.service.cohorte.CohorteServiceImpl;
import com.app.idoneos.service.curso.CursoServiceImpl;
import com.app.idoneos.service.inscripcion.InscripcionServiceImpl;
import com.app.idoneos.service.pago.PagoService;
import com.app.idoneos.service.pago.PagoServiceImpl;
import com.app.idoneos.service.pasarelapago.PasarelaPagoService;
import com.app.idoneos.service.pago.PagoService.SolicitudPago;
import com.app.idoneos.service.programa.ProgramaServiceImpl;
import com.app.idoneos.support.PasarelaPagoMockeada;

/**
 * Pruebas de las reglas de negocio de los pagos (CU-50 a CU-54), con la pasarela de pago mockeada (ver PasarelaPagoMockeada) y sobre los
 * datos de la semilla. Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 *
 * En la semilla, Lucía tiene su inscripción a Finanzas Personales ($ 80.000) pendiente de pago, con un pago de MODO
 * rechazado por fondos insuficientes; el resto de las inscripciones con costo tienen su pago aprobado.
 */
@SpringBootTest
@Transactional
class PagoServiceTest {

    private static final String LUCIA = "lucia.fernandez@correo.com";
    private static final String MARTIN = "martin.gomez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private PagoServiceImpl pagoService;

    @Autowired
    private InscripcionServiceImpl inscripcionService;

    @MockitoBean
    private PasarelaPagoService pasarelaMock;

    private PasarelaPagoMockeada pasarela;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private EstadoPagoRepository estadoPagoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private CohorteServiceImpl cohorteService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private ProgramaServiceImpl programaService;

    @BeforeEach
    void configurarPasarela() {
        pasarela = new PasarelaPagoMockeada(pasarelaMock);
    }

    private Inscripcion pendienteDeLucia() {
        return inscripcionService.buscarConFiltros(null, LUCIA, Inscripcion.ACCESO_PENDIENTE_DE_PAGO).get(0);
    }

    private Alumno alumno(String correo) {
        return inscripcionService.buscarConFiltros(null, correo, null).get(0).getAlumno();
    }

    /** Inscribe a un alumno a Finanzas Personales ($ 80.000), que queda pendiente de pago. */
    private Inscripcion inscribirAFinanzas(String correo) {
        return inscribirA("Finanzas Personales e Inversión", correo);
    }

    /** Inscribe a un alumno a la cohorte abierta de un curso con costo, que queda pendiente de pago. */
    private Inscripcion inscribirA(String curso, String correo) {
        Integer idPrograma = programaService.buscarPorCurso(cursoService.buscarPorNombre(curso).orElseThrow())
                .get(0).getIdPrograma();
        Integer idCohorte = cohorteService.buscarConFiltros(idPrograma, null, "Abierta", null, null, "recientes", null)
                .get(0).getIdCohorte();
        return inscripcionService.inscribir(alumno(correo), idCohorte, true);
    }

    private Descuento descuento(String nombre) {
        return descuentoRepository.findByNombre(nombre).orElseThrow();
    }

    private PasarelaPagoService.Resultado rechazado(String motivo) {
        return new PasarelaPagoService.Resultado(PasarelaPagoService.Estado.RECHAZADO, null, "Tarjeta de crédito Visa", "0008", null, null,
                null, motivo, null);
    }

    // ---------- Descuento que le corresponde al pagar ----------

    @Test
    @DisplayName("CU-51: el descuento sin condición se aplica a todos y se calcula el monto con él")
    void descuentoSinCondicion() {
        Inscripcion lucia = pendienteDeLucia();

        // Lucía compró 1 curso (Mercado de Capitales): le corresponde el de bienvenida (10%), no el de 2 cursos
        assertEquals("Descuento de bienvenida", pagoService.calcularDescuento(lucia).orElseThrow().getNombre());
        assertEquals(72000f, pagoService.calcularMontoAPagar(lucia));
    }

    @Test
    @DisplayName("CU-51: aplica un solo descuento, el de mayor porcentaje entre los que cumple")
    void descuentoDeMayorPorcentaje() {
        // Valentina compró 2 cursos (Mercado y Análisis): cumple "Alumno frecuente" (20%) y el de bienvenida (10%)
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);

        assertEquals("Alumno frecuente", pagoService.calcularDescuento(nueva).orElseThrow().getNombre());
        assertEquals(64000f, pagoService.calcularMontoAPagar(nueva));
    }

    @Test
    @DisplayName("CU-51: no aplica descuentos vencidos, agotados, próximos ni dados de baja")
    void descuentosNoVigentes() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);
        descuento("Alumno frecuente").setCantidadUsada(50); // agotado
        assertEquals("Descuento de bienvenida", pagoService.calcularDescuento(nueva).orElseThrow().getNombre());

        descuento("Descuento de bienvenida").marcarInactivo();
        assertTrue(pagoService.calcularDescuento(nueva).isEmpty());
        assertEquals(80000f, pagoService.calcularMontoAPagar(nueva));
    }

    @Test
    @DisplayName("CU-51: los cursos comprados son las inscripciones habilitadas con pago aprobado")
    void cursosComprados() {
        // Valentina compró 2 cursos y Lucía solo 1: el descuento de 2 cursos le corresponde a Valentina y no a Lucía
        Inscripcion deValentina = inscribirAFinanzas(VALENTINA);
        assertEquals("Alumno frecuente", pagoService.calcularDescuento(deValentina).orElseThrow().getNombre());
        assertNotEquals("Alumno frecuente", pagoService.calcularDescuento(pendienteDeLucia()).orElseThrow().getNombre());

        // Si el descuento exige 3 cursos comprados, Valentina (que compró 2) deja de cumplirlo
        descuento("Alumno frecuente").setCursosRequeridos(3);
        assertNotEquals("Alumno frecuente", pagoService.calcularDescuento(deValentina).orElseThrow().getNombre());
    }

    // ---------- CU-51 Realizar pago / CU-52 Reintentar pago ----------

    @Test
    @DisplayName("CU-52: reintentar reutiliza el registro del pago rechazado, con un identificador de intención nuevo")
    void reintentaReutilizandoElRegistro() {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();
        assertTrue(rechazado.estaRechazado());
        String idExternoAnterior = rechazado.getIdIntencionExterna();

        SolicitudPago solicitud = pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno());

        assertEquals(rechazado.getIdPago(), solicitud.idPago());
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

        SolicitudPago solicitud = pagoService.iniciar(nueva.getIdInscripcion(), nueva.getAlumno());

        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();
        assertTrue(pago.estaPendiente());
        assertEquals(MetodoPago.MODO, pago.getMetodoPago().getNombre());
        assertEquals(64000f, pago.getMonto());
        assertTrue(pago.getIdIntencionExterna().startsWith("IDN-"));
        assertTrue(solicitud.idSolicitud().startsWith("SOL-"));
        assertNotNull(solicitud.idSolicitud());
        assertEquals(0, descuento("Alumno frecuente").getCantidadUsada()); // el uso se cuenta al aprobarse el pago
        assertFalse(nueva.estaHabilitada()); // el acceso sigue sin habilitar hasta que se apruebe
    }

    @Test
    @DisplayName("CU-51: con el pago aprobado se habilita el acceso, se cuenta el uso del descuento y se emite el comprobante")
    void apruebaElPago() {
        Inscripcion lucia = pendienteDeLucia();
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        Pago pago = pagoService.consultarEstado(solicitud.idPago(), lucia.getAlumno()); // la plataforma aprueba

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
        assertEquals("Pagador de Prueba", pago.getComprobanteVigente().getNombrePagador());
        assertEquals("30111222", pago.getComprobanteVigente().getDniPagador());
        assertFalse(pago.getComprobanteVigente().getEnviado()); // el envío por correo es de MOD-NF-01
    }

    @Test
    @DisplayName("CU-51: un pago rechazado queda rechazado con el motivo, no habilita el acceso ni cuenta el descuento")
    void rechazaElPago() {
        Inscripcion lucia = pendienteDeLucia();
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        pasarela.resolver(solicitud.idSolicitud(), rechazado("INSUFFICIENT_FUNDS"));

        Pago pago = pagoService.consultarEstado(solicitud.idPago(), lucia.getAlumno());

        assertTrue(pago.estaRechazado());
        assertEquals("INSUFFICIENT_FUNDS", pago.getDetalleErrorOperacion());
        assertFalse(lucia.estaHabilitada());
        assertEquals(usosAntes, descuento("Descuento de bienvenida").getCantidadUsada());
        assertNull(pago.getComprobanteVigente());
        // y se puede reintentar de nuevo con el mismo registro
        assertEquals(solicitud.idPago(), pagoService.reintentar(pago.getIdPago(), lucia.getAlumno()).idPago());
    }

    @Test
    @DisplayName("CU-51: sin confirmación el pago sigue pendiente y, vencido el plazo, se rechaza")
    void vencePorFaltaDeConfirmacion() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();

        assertTrue(pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno()).estaPendiente());

        pago.setUltimaModificacion(LocalDateTime.now().minusMinutes(pagoService.plazoConfirmacionMinutos() + 1));
        pago = pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno());

        assertTrue(pago.estaRechazado());
        assertTrue(pago.getDetalleErrorOperacion().contains("no se confirmó dentro del plazo"));
        assertFalse(lucia.estaHabilitada());
    }

    @Test
    @DisplayName("CU-51: si MODO ya aprobó el pago, vence el plazo pero gana el resultado de MODO")
    void elResultadoDeModoGanaAlPlazo() {
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();
        pago.setUltimaModificacion(LocalDateTime.now().minusMinutes(30));

        assertTrue(pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno()).estaAprobado());
    }

    @Test
    @DisplayName("CU-51: volver a pagar con un pago en curso retoma su solicitud en lugar de crear otra")
    void retomaElPagoEnCurso() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago primera = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        SolicitudPago segunda = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        assertEquals(primera.idPago(), segunda.idPago());
        assertEquals(primera.idSolicitud(), segunda.idSolicitud());
        assertNotNull(segunda.idSolicitud());
        assertEquals(1, lucia.getPagos().size());
    }

    @Test
    @DisplayName("CU-46: al registrar la inscripción, si el descuento del alumno cubre todo el precio queda habilitada sin pago")
    void administradorHabilitaSiNoHayQuePagar() {
        Descuento beca = descuentoRepository.save(new Descuento("Beca completa", 0, 100, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), 5, 0));
        Inscripcion lucia = pendienteDeLucia();

        assertTrue(pagoService.habilitarSiNoHayQuePagar(lucia));

        assertTrue(lucia.estaHabilitada());
        assertEquals(1, beca.getCantidadUsada());
        // sin descuento que cubra todo, no hace nada
        Inscripcion otra = inscribirAFinanzas(VALENTINA);
        beca.setBaja(true);
        descuentoRepository.save(beca);
        assertTrue(!pagoService.habilitarSiNoHayQuePagar(otra));
        assertTrue(!otra.estaHabilitada());
    }

    @Test
    @DisplayName("CU-51: si el descuento cubre todo el precio, se habilita el acceso sin registrar ningún pago ni comprobante")
    void descuentoTotal() {
        descuentoRepository.save(new Descuento("Beca completa", 0, 100, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), 5, 0));
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazadoAntes = lucia.getPagoSinAprobar();
        long pagosAntes = pagoRepository.count();

        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        assertTrue(solicitud.aprobado());
        assertNull(solicitud.idPago());
        assertNull(solicitud.idSolicitud());
        assertTrue(lucia.estaHabilitada());
        assertEquals(Inscripcion.ACCESO_VIGENTE, lucia.getEstadoAcceso());
        assertEquals(pagosAntes, pagoRepository.count()); // no se registró ningún pago
        assertNull(lucia.getPagoAprobado());
        assertTrue(rechazadoAntes.estaRechazado()); // el intento anterior queda como estaba
    }

    @Test
    @DisplayName("CU-51: el descuento que cubre todo el precio cuenta su uso y, al agotarse, deja de aplicarse")
    void descuentoTotalCuentaSuUso() {
        descuentoRepository.save(new Descuento("Beca de uno", 0, 100, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), 1, 0));
        Inscripcion lucia = pendienteDeLucia();

        pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        assertEquals(1, descuento("Beca de uno").getCantidadUsada());
        assertTrue(descuento("Beca de uno").estaAgotado());
        // Con el cupo agotado la beca ya no se aplica: a Valentina (2 cursos comprados) le corresponde el de 2 cursos
        Inscripcion otra = inscribirA("Contabilidad Básica para Inversores", VALENTINA);
        assertEquals("Alumno frecuente", pagoService.calcularDescuento(otra).orElseThrow().getNombre());
        assertEquals(36000f, pagoService.calcularMontoAPagar(otra)); // $ 45.000 con el 20%
    }

    @Test
    @DisplayName("CU-51: la inscripción habilitada por un descuento total no cuenta como curso comprado")
    void descuentoTotalNoCuentaComoCursoComprado() {
        descuentoRepository.save(new Descuento("Beca de uno", 0, 100, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), 1, 0));
        Inscripcion lucia = pendienteDeLucia();
        pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno()); // queda habilitada, sin pago

        // Lucía compró 1 curso (Mercado de Capitales). Si la inscripción sin pago contara, tendría 2 y le
        // correspondería "Alumno frecuente" (20%); le corresponde solo el descuento de bienvenida (10%)
        Inscripcion otra = inscribirA("Contabilidad Básica para Inversores", LUCIA);

        assertEquals("Descuento de bienvenida", pagoService.calcularDescuento(otra).orElseThrow().getNombre());
    }

    @Test
    @DisplayName("CU-51: no se crea una solicitud por un monto menor al mínimo de MODO")
    void montoMenorAlMinimo() {
        Inscripcion lucia = pendienteDeLucia();
        lucia.getCurso().setPrecio(20f);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno()));
        assertTrue(error.getMessage().contains("mínimo"));
    }

    @Test
    @DisplayName("CU-51: solo el dueño paga una inscripción que no esté habilitada, paga ni vencida")
    void validacionesDelPago() {
        Inscripcion lucia = pendienteDeLucia();
        assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(lucia.getIdInscripcion(), alumno(MARTIN)));
        assertThrows(IllegalArgumentException.class, () -> pagoService.iniciar(lucia.getIdInscripcion(), null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.iniciar(99999, lucia.getAlumno()));

        Inscripcion deMartin = inscripcionService.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0);
        IllegalArgumentException yaPaga = assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(deMartin.getIdInscripcion(), deMartin.getAlumno()));
        assertTrue(yaPaga.getMessage().contains("ya está aprobado"));

        // el cupo se ocupa al habilitarse: con la cohorte llena no se puede pagar
        lucia.getCohorte().setCupoMaximo(lucia.getCohorte().getCuposOcupados());
        IllegalArgumentException sinCupo = assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno()));
        assertTrue(sinCupo.getMessage().contains("cupo máximo"));
    }

    @Test
    @DisplayName("CU-52: solo se reintenta un pago rechazado, de una inscripción sin acceso, y propio")
    void validacionesDelReintento() {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();
        assertThrows(IllegalArgumentException.class, () -> pagoService.reintentar(rechazado.getIdPago(), alumno(MARTIN)));

        pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno()); // ahora está pendiente
        IllegalArgumentException pendiente = assertThrows(IllegalArgumentException.class,
                () -> pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno()));
        assertTrue(pendiente.getMessage().contains("no se encuentra rechazado"));
    }

    @Test
    @DisplayName("CU-51: el aviso de MODO identifica el pago por su solicitud o su identificador externo y se concilia")
    void procesaElAvisoDeModo() {
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();

        assertFalse(pagoService.procesarNotificacion("SOL-desconocida", null));
        assertFalse(pagoService.procesarNotificacion(null, null));
        assertTrue(pagoService.procesarNotificacion(null, pago.getIdIntencionExterna()));

        assertTrue(pago.estaAprobado());
    }

    @Test
    @DisplayName("CU-51: la conciliación periódica resuelve los pagos pendientes")
    void conciliaLosPendientes() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        assertEquals(0, pagoService.conciliarPendientes());

        pasarela.aprobarTodo();
        assertEquals(1, pagoService.conciliarPendientes());
        assertTrue(pagoRepository.findById(solicitud.idPago()).orElseThrow().estaAprobado());
    }

    // ---------- CU-53 Registrar pago ----------

    @Test
    @DisplayName("CU-54: en un pago de MODO el código de referencia son letras y números, y el número de comprobante, solo números")
    void formatosDeUnPagoDeModo() {
        Inscripcion lucia = pendienteDeLucia();
        Integer id = lucia.getPagoSinAprobar().getIdPago();
        String comprobante = "123456789012345678901234567890";

        IllegalArgumentException guion = assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id,
                "Aprobado", 72000f, "QR-1234", comprobante, "Lucía Fernández", "40123456", null));
        assertTrue(guion.getMessage().contains("código de referencia no es válido"));
        IllegalArgumentException letras = assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id,
                "Aprobado", 72000f, "QRTEST09", "ABC-123456789", "Lucía Fernández", "40123456", null));
        assertTrue(letras.getMessage().contains("comprobante no es válido"));
        // un código de 5 caracteres no llega al mínimo: el mensaje dice cuál es el formato válido
        IllegalArgumentException corto = assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id,
                "Aprobado", 72000f, "39933", comprobante, "Lucía Fernández", "40123456", null));
        assertTrue(corto.getMessage().contains("Formato válido: letras y números, sin guiones, de 6 a 12 caracteres"));
        // un comprobante de MODO de menos de 4 dígitos no es válido y el mensaje dice el formato
        IllegalArgumentException cortoModo = assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id,
                "Aprobado", 72000f, "QRTEST08", "123", "Lucía Fernández", "40123456", null));
        assertTrue(cortoModo.getMessage().contains("Formato válido: solo números, de 4 a 100 dígitos"));
        // se normaliza a mayúsculas
        assertEquals("QRTEST09", pagoService.modificar(id, "Aprobado", 72000f, "qrtest09", "5656546",
                "Lucía Fernández", "40123456", null).getCodigoReferencia());
    }

    @Test
    @DisplayName("CU-51: si el monto a cobrar cambió respecto del que el alumno vio en la caja, no se cobra y se avisa")
    void avisaSiCambioElMonto() {
        Inscripcion lucia = pendienteDeLucia();
        float actual = pagoService.calcularMontoAPagar(lucia);

        PagoService.MontoCambiadoException error = assertThrows(PagoService.MontoCambiadoException.class,
                () -> pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno(), actual + 8000f));
        assertTrue(error.getMessage().contains("cambió"));
        // con el monto que corresponde, se inicia normalmente
        assertTrue(pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno(), actual).idSolicitud() != null);
    }

    @Test
    @DisplayName("CU-53: un pago aprobado que no cubre el precio del curso no habilita la inscripción")
    void pagoParcialNoHabilita() {
        Inscripcion lucia = pendienteDeLucia();

        Pago pago = pagoService.registrarManual(lucia.getIdInscripcion(), 1000f, "TRF-000111", null);

        assertTrue(pago.estaAprobado());
        assertTrue(!lucia.estaHabilitada());
        assertTrue(!pagoService.cubreElPrecio(lucia));
    }

    @Test
    @DisplayName("CU-53: registra el pago por transferencia: queda aprobado, habilita el acceso y emite el comprobante")
    void registraUnPagoManual() {
        Inscripcion lucia = pendienteDeLucia();
        Pago anterior = lucia.getPagoSinAprobar(); // el rechazado de MODO se reutiliza

        Pago pago = pagoService.registrarManual(lucia.getIdInscripcion(), 72000f, " TRF-778899 ", "Transferencia del 12/10");

        assertEquals(anterior.getIdPago(), pago.getIdPago());
        assertTrue(pago.estaAprobado());
        assertEquals(MetodoPago.TRANSFERENCIA, pago.getMetodoPago().getNombre());
        assertEquals("TRF-778899", pago.getNumeroComprobante());
        assertTrue(pago.getDetalleMetodoPago().contains("Transferencia del 12/10"));
        assertNull(pago.getDescuento());
        assertNull(pago.getDetalleErrorOperacion());
        assertTrue(lucia.estaHabilitada());
        assertEquals("Lucía Fernández", pago.getComprobanteVigente().getNombrePagador());
        assertEquals("40123456", pago.getComprobanteVigente().getDniPagador());
    }

    @Test
    @DisplayName("CU-53: al registrar un pago aprobado, el pago pendiente de la inscripción queda rechazado y se crea uno nuevo")
    void registrarUnPagoRechazaElPendiente() {
        Inscripcion lucia = pendienteDeLucia();
        Pago pendiente = lucia.getPagoSinAprobar();
        pendiente.setEstadoPago(estadoPagoRepository.findByNombre(EstadoPago.PENDIENTE).orElseThrow());
        pagoRepository.save(pendiente);
        assertTrue(pendiente.estaPendiente());

        Pago pago = pagoService.registrarManual(lucia.getIdInscripcion(), 72000f, "TRF-778899", null);

        assertTrue(pago.estaAprobado());
        assertNotEquals(pendiente.getIdPago(), pago.getIdPago());
        assertTrue(pendiente.estaRechazado());
        assertNull(pendiente.getDetalleErrorOperacion());
        assertTrue(lucia.estaHabilitada());
        assertEquals(pago.getIdPago(), lucia.getPagoAprobado().getIdPago());
    }

    @Test
    @DisplayName("CU-53: informa los datos obligatorios que faltan y los inválidos")
    void validacionesDelPagoManual() {
        Inscripcion lucia = pendienteDeLucia();
        Integer id = lucia.getIdInscripcion();

        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(id, null, "TRF-1", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(id, 100f, " ", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(id, 0f, "TRF-1", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(id, 100f, "X".repeat(101), null));
        // solo letras, números y guiones
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(id, 100f, "TRF 1", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(id, 100f, "TRF-1", "x".repeat(301)));
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(99999, 100f, "TRF-1", null));
        IllegalArgumentException repetido = assertThrows(IllegalArgumentException.class,
                () -> pagoService.registrarManual(id, 100f, "TRF-000451", null)); // el de la semilla
        assertTrue(repetido.getMessage().contains("Ya existe un pago"));
        assertFalse(lucia.estaHabilitada());
    }

    @Test
    @DisplayName("CU-53: no se registra el pago de una inscripción que ya está paga")
    void pagoManualDeUnaInscripcionPaga() {
        Inscripcion deMartin = inscripcionService.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0);

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.registrarManual(deMartin.getIdInscripcion(), 100f, "TRF-NUEVO", null));
    }

    @Test
    @DisplayName("CU-53: una inscripción nueva sin pagos recibe un registro de pago nuevo")
    void pagoManualDeUnaInscripcionNueva() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);

        Pago pago = pagoService.registrarManual(nueva.getIdInscripcion(), pagoService.calcularMontoPendiente(nueva),
                "TRF-900001", null);

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

        Pago pago = pagoService.modificar(rechazado.getIdPago(), "Aprobado", 72000f, "QRTEST001", "123456789012345678901234567890", "Lucía Fernández", "40123456", null);

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
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        pasarela.resolver(solicitud.idSolicitud(), rechazado("EXPIRED_CARD"));
        pagoService.consultarEstado(solicitud.idPago(), lucia.getAlumno());
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();

        pagoService.modificar(solicitud.idPago(), "Aprobado", 72000f, "QRTEST002", "123456789012345678901234567890", "Lucía Fernández", "40123456", null);

        assertEquals(usosAntes + 1, descuento("Descuento de bienvenida").getCantidadUsada());
    }

    @Test
    @DisplayName("CU-54: exige el nombre y el DNI del pagador para aprobar un pago de MODO")
    void exigeElPagadorDeModo() {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        Integer id = rechazado.getIdPago();

        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", 72000f, "QRTEST003", "123456789012345678901234567890", "", "40123456", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", 72000f, "QRTEST003", "123456789012345678901234567890", "Lucía", "abc", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", 72000f, "QRTEST003", "123456789012345678901234567890", "Lucía", null, null));
        assertTrue(rechazado.estaRechazado());
    }

    @Test
    @DisplayName("CU-54: marca un pago pendiente como rechazado, con el motivo")
    void rechazaManualmente() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        Pago pago = pagoService.modificar(solicitud.idPago(), "Rechazado", 72000f, null, null, null, null, "No se acreditó");

        assertTrue(pago.estaRechazado());
        assertEquals("No se acreditó", pago.getDetalleErrorOperacion());
        assertFalse(lucia.estaHabilitada());
        assertEquals("Rechazado por el administrador.",
                pagoService.modificar(solicitud.idPago(), "Rechazado", 72000f, null, null, null, null, " ").getDetalleErrorOperacion());
    }

    @Test
    @DisplayName("CU-54: solo se modifica un pago pendiente o rechazado, con estado y monto válidos")
    void validacionesDeModificarPago() {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        Pago aprobado = inscripcionService.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0).getPagos().iterator().next();

        IllegalArgumentException estado = assertThrows(IllegalArgumentException.class,
                () -> pagoService.modificar(aprobado.getIdPago(), "Rechazado", 100f, null, "TRF-1", null, null, null));
        assertTrue(estado.getMessage().contains("pendiente o rechazado"));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(rechazado.getIdPago(), "Pendiente", 100f, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(rechazado.getIdPago(), null, 100f, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(rechazado.getIdPago(), "Rechazado", null, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(rechazado.getIdPago(), "Rechazado", -5f, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(99999, "Aprobado", 100f, null, null, null, null, null));
    }

    @Test
    @DisplayName("CU-54: un pago por transferencia exige su comprobante y no admite uno repetido")
    void transferenciaExigeComprobante() {
        Inscripcion nueva = inscribirAFinanzas(VALENTINA);
        Pago transferencia = pagoRepository.save(new Pago(estadoPagoRepository.findByNombre(EstadoPago.PENDIENTE).orElseThrow(),
                metodoPagoRepository.findByNombre(MetodoPago.TRANSFERENCIA).orElseThrow(), nueva, "IDN-PRUEBA-TRF", pagoService.calcularMontoPendiente(nueva)));
        Integer id = transferencia.getIdPago();
        float monto = pagoService.calcularMontoPendiente(nueva);

        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", monto, null, " ", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", monto, null, "TRF-000451", null, null, null));

        Pago pago = pagoService.modificar(id, "Aprobado", monto, null, "TRF-123456", null, null, null);

        assertTrue(pago.estaAprobado());
        assertEquals("Valentina Ruiz", pago.getComprobanteVigente().getNombrePagador());
    }

    // ---------- CU-50 Buscar pago ----------

    @Test
    @DisplayName("CU-50: busca por estado, curso, alumno y fechas")
    void buscaPagos() {
        assertEquals(6, pagoService.buscarConFiltros(null, null, "Aprobado", null, null, null).size());
        assertEquals(1, pagoService.buscarConFiltros(null, null, "rechazado", null, null, null).size());
        assertEquals(0, pagoService.buscarConFiltros(null, null, "Pendiente", null, null, null).size());
        assertEquals(7, pagoService.buscarConFiltros(null, null, null, null, null, null).size());
        assertEquals(3, pagoService.buscarConFiltros(null, "martin", null, null, null, null).size());
        assertEquals(1, pagoService.buscarConFiltros(null, "40123456", "Rechazado", null, null, null).size());

        Integer idCurso = cursoService.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow().getIdCurso();
        assertEquals(3, pagoService.buscarConFiltros(idCurso, null, null, null, null, null).size());

        LocalDate hoy = LocalDate.now();
        assertEquals(2, pagoService.buscarConFiltros(null, null, null, hoy.minusDays(1), hoy, null).size()); // el de ayer y el de hace 3 horas
        assertEquals(1, pagoService.buscarConFiltros(null, null, null, hoy.minusDays(60), hoy.minusDays(40), null).size());
    }

    @Test
    @DisplayName("CU-50: el alumno ve solo sus pagos, del más reciente al más antiguo")
    void elAlumnoVeSoloSusPagos() {
        List<Pago> deLucia = pagoService.buscarConFiltros(null, null, null, null, null, alumno(LUCIA));

        assertEquals(2, deLucia.size());
        assertTrue(deLucia.stream().allMatch(p -> p.getInscripcion().getAlumno().getUsuario().getCorreo().equals(LUCIA)));
        assertTrue(deLucia.get(0).estaRechazado()); // el de hace 3 horas va primero
        assertEquals(2, pagoService.buscarConFiltros(null, null, null, null, null, alumno(VALENTINA)).size());
    }

    @Test
    @DisplayName("CU-50: el alumno no accede al pago de otro, el administrador sí")
    void pagoVisible() {
        Pago deLucia = pendienteDeLucia().getPagoSinAprobar();

        assertEquals(deLucia, pagoService.buscarVisible(deLucia.getIdPago(), alumno(LUCIA)));
        assertEquals(deLucia, pagoService.buscarVisible(deLucia.getIdPago(), null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.buscarVisible(deLucia.getIdPago(), alumno(MARTIN)));
        assertThrows(IllegalArgumentException.class, () -> pagoService.buscarVisible(99999, null));
    }

    @Test
    @DisplayName("El plazo de confirmación del pago se toma del parámetro y se limita a lo que admite MODO (5 a 10 minutos)")
    void plazoDeConfirmacion() {
        assertEquals(10, pagoService.plazoConfirmacionMinutos());
    }
}
