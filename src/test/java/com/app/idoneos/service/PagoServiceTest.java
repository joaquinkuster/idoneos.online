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

/** Pruebas de las reglas de negocio de los pagos. */
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
        return inscripcionService.buscarConFiltros(null, LUCIA, com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO).get(0);
    }

    /** Inicia el pago de Lucía con MODO y lo deja pendiente: el pago rechazado de la semilla ya no se modifica. */
    private Pago pagoPendienteDeLucia() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        return pagoRepository.findById(solicitud.idPago()).orElseThrow();
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
        Integer idCohorte = cohorteService.buscarConFiltros(idPrograma, null, com.app.idoneos.model.EstadoCohorte.ABIERTA, null, null, "recientes", null)
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

    // ----------------------------------------------------------------------
    // Descuento que le corresponde al pagar ----------
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Realizar pago / Reintentar pago ----------
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-52: reintentar crea un pago nuevo; el rechazado conserva su identificador de intención y su motivo")
    void reintentaConUnPagoNuevo() {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();
        assertTrue(rechazado.estaRechazado());
        String idExternoAnterior = rechazado.getIdIntencionExterna();
        String motivoAnterior = rechazado.getMotivoRechazo();

        SolicitudPago solicitud = pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno());

        // el intento nuevo es otro pago, pendiente y con su propia solicitud
        assertNotEquals(rechazado.getIdPago(), solicitud.idPago());
        assertFalse(solicitud.aprobado());
        Pago nuevo = pagoRepository.findById(solicitud.idPago()).orElseThrow();
        assertTrue(nuevo.estaPendiente());
        assertNotEquals(idExternoAnterior, nuevo.getIdIntencionExterna());
        assertEquals(solicitud.idSolicitud(), nuevo.getIdSolicitudPago());
        assertEquals(72000f, nuevo.getMonto());
        assertEquals("Descuento de bienvenida", nuevo.getDescuento().getNombre());
        assertNull(nuevo.getMotivoRechazo());
        // y el rechazado queda como estaba, para poder rastrearlo
        assertTrue(rechazado.estaRechazado());
        assertEquals(idExternoAnterior, rechazado.getIdIntencionExterna());
        assertEquals(motivoAnterior, rechazado.getMotivoRechazo());
        // la inscripción reserva su lugar mientras el pago está en curso
        assertTrue(lucia.estaConPagoEnCurso());
        assertTrue(lucia.ocupaCupo());
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
        assertFalse(nueva.fueHabilitada()); // el acceso sigue sin habilitar hasta que se apruebe
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
        assertTrue(lucia.fueHabilitada());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.HABILITADA, lucia.getNombreEstado());
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
        assertEquals("INSUFFICIENT_FUNDS", pago.getMotivoRechazo());
        assertFalse(lucia.fueHabilitada());
        assertEquals(usosAntes, descuento("Descuento de bienvenida").getCantidadUsada());
        assertNull(pago.getComprobanteVigente());
        // la inscripción vuelve a "Pendiente de pago" y libera el lugar que reservaba
        assertTrue(lucia.estaPendienteDePago());
        assertFalse(lucia.ocupaCupo());
        // y se puede reintentar con un pago nuevo
        assertNotEquals(solicitud.idPago(), pagoService.reintentar(pago.getIdPago(), lucia.getAlumno()).idPago());
    }

    @Test
    @DisplayName("CU-51: sin pago el pago sigue pendiente hasta el vencimiento más el margen y entonces, con la respuesta de MODO, se rechaza")
    void vencePorFaltaDePago() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();

        assertTrue(pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno()).estaPendiente());
        assertFalse(pagoService.estaVerificando(pago));

        // Vencida la solicitud pero dentro del margen: se está verificando con MODO, no se rechaza todavía y la inscripción conserva su lugar
        pago.setFechaCreacion(LocalDateTime.now().minusSeconds(PasarelaPagoMockeada.VIGENCIA_SEGUNDOS + 60));
        assertTrue(pagoService.estaVerificando(pago));
        assertTrue(pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno()).estaPendiente());
        assertTrue(lucia.estaConPagoEnCurso());
        // y mientras tanto no se puede iniciar otro pago
        IllegalArgumentException verificando = assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno()));
        assertTrue(verificando.getMessage().contains("verificando tu pago"), verificando.getMessage());

        // Pasado el margen, MODO informa que nadie pagó (Created o Scanned): se rechaza por vencimiento
        pago.setFechaCreacion(LocalDateTime.now().minusSeconds(PasarelaPagoMockeada.VIGENCIA_SEGUNDOS + 3 * 60));
        pago = pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno());

        assertTrue(pago.estaRechazado());
        assertTrue(pago.getMotivoRechazo().contains("no se realizó dentro del plazo"));
        assertFalse(lucia.fueHabilitada());
        assertTrue(lucia.estaPendienteDePago());
        assertFalse(lucia.ocupaCupo());
    }

    @Test
    @DisplayName("CU-51: mientras MODO diga Processing el pago no se rechaza, aunque haya pasado el vencimiento y el margen")
    void procesandoNoSeRechaza() {
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        pasarela.resolver(solicitud.idSolicitud(),
                PasarelaPagoService.Resultado.procesando(new PasarelaPagoService.Solicitud(solicitud.idSolicitud()), null));
        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();
        pago.setFechaCreacion(LocalDateTime.now().minusMinutes(60));

        pago = pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno());

        assertTrue(pago.estaPendiente());
        assertTrue(lucia.estaConPagoEnCurso());
        assertTrue(lucia.ocupaCupo());
    }

    @Test
    @DisplayName("CU-51: si MODO no responde no se decide nada: el pago sigue pendiente y la inscripción conserva su lugar")
    void sinRespuestaDeModoNoSeRechaza() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        Pago pago = pagoRepository.findById(solicitud.idPago()).orElseThrow();
        pago.setFechaCreacion(LocalDateTime.now().minusMinutes(60));
        pasarela.fallarConsultas();

        pago = pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno());

        assertTrue(pago.estaPendiente());
        assertTrue(lucia.estaConPagoEnCurso());

        // cuando MODO responde, aunque sea tarde, un pago aprobado habilita la inscripción
        pasarela.aprobarTodo();
        pago = pagoService.consultarEstado(pago.getIdPago(), lucia.getAlumno());
        assertTrue(pago.estaAprobado());
        assertTrue(lucia.tieneAcceso());
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
        // el rechazado anterior más el que está en curso (que no se duplica)
        assertEquals(2, lucia.getPagos().size());
        assertEquals(primera.idPago(), lucia.getPagoEnCurso().getIdPago());
    }

    @Test
    @DisplayName("CU-46: al registrar la inscripción, si el descuento del alumno cubre todo el precio queda habilitada sin pago")
    void administradorHabilitaSiNoHayQuePagar() {
        Descuento beca = descuentoRepository.save(new Descuento("Beca completa", 0, 100, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10), 5, 0));
        Inscripcion lucia = pendienteDeLucia();

        assertTrue(pagoService.habilitarSiNoHayQuePagar(lucia));

        assertTrue(lucia.fueHabilitada());
        assertEquals(1, beca.getCantidadUsada());
        // sin descuento que cubra todo, no hace nada
        Inscripcion otra = inscribirAFinanzas(VALENTINA);
        beca.setBaja(true);
        descuentoRepository.save(beca);
        assertTrue(!pagoService.habilitarSiNoHayQuePagar(otra));
        assertTrue(!otra.fueHabilitada());
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
        assertTrue(lucia.fueHabilitada());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.HABILITADA, lucia.getNombreEstado());
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
        // correspondería "Alumno frecuente" (20%). Le corresponde solo el descuento de bienvenida (10%)
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

        Inscripcion deMartin = inscripcionService.buscarConFiltros(null, MARTIN, com.app.idoneos.model.EstadoInscripcion.HABILITADA).get(0);
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

        SolicitudPago nueva = pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno()); // el intento nuevo queda en curso
        // reintentar de nuevo retoma ese pago en curso en lugar de abrir otro
        assertEquals(nueva.idPago(), pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno()).idPago());
        // y el pago en curso no se reintenta: solo se reintenta uno rechazado
        IllegalArgumentException pendiente = assertThrows(IllegalArgumentException.class,
                () -> pagoService.reintentar(nueva.idPago(), lucia.getAlumno()));
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

    // ----------------------------------------------------------------------
    // Registrar pago ----------
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-54: en un pago de MODO el código de referencia son letras y números, y el número de comprobante, solo números")
    void formatosDeUnPagoDeModo() {
        Integer id = pagoPendienteDeLucia().getIdPago();
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
        assertTrue(!lucia.fueHabilitada());
        assertTrue(!pagoService.cubreElPrecio(lucia));
    }

    @Test
    @DisplayName("CU-53: registra el pago por transferencia: queda aprobado, habilita el acceso y emite el comprobante")
    void registraUnPagoManual() {
        Inscripcion lucia = pendienteDeLucia();
        Pago anterior = lucia.getPagoSinAprobar(); // el rechazado de MODO se conserva

        Pago pago = pagoService.registrarManual(lucia.getIdInscripcion(), 72000f, " TRF-778899 ", "Transferencia del 12/10");

        assertNotEquals(anterior.getIdPago(), pago.getIdPago()); // la transferencia es un pago nuevo
        assertTrue(anterior.estaRechazado());
        assertEquals(MetodoPago.MODO, anterior.getMetodoPago().getNombre());
        assertTrue(pago.estaAprobado());
        assertEquals(MetodoPago.TRANSFERENCIA, pago.getMetodoPago().getNombre());
        assertEquals("TRF-778899", pago.getNumeroComprobante());
        assertTrue(pago.getDetalleMetodoPago().contains("Transferencia del 12/10"));
        assertNull(pago.getDescuento());
        assertNull(pago.getMotivoRechazo());
        assertTrue(lucia.fueHabilitada());
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
        assertEquals("Se aprobó otro pago de esta inscripción.", pendiente.getMotivoRechazo());
        assertTrue(lucia.fueHabilitada());
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
        assertFalse(lucia.fueHabilitada());
    }

    @Test
    @DisplayName("CU-53: no se registra el pago de una inscripción que ya está paga")
    void pagoManualDeUnaInscripcionPaga() {
        Inscripcion deMartin = inscripcionService.buscarConFiltros(null, MARTIN, com.app.idoneos.model.EstadoInscripcion.HABILITADA).get(0);

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
        assertTrue(nueva.fueHabilitada());
        assertEquals(1, nueva.getPagos().size());
    }

    // ----------------------------------------------------------------------
    // Modificar pago ----------
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-54: acredita un pago de MODO pendiente indicando el pagador: habilita el acceso y emite el comprobante")
    void acreditaUnPagoDeModo() {
        Inscripcion lucia = pendienteDeLucia();
        Pago pendiente = pagoPendienteDeLucia();

        Pago pago = pagoService.modificar(pendiente.getIdPago(), "Aprobado", 72000f, "QRTEST001", "123456789012345678901234567890", "Lucía Fernández", "40123456", null);

        assertTrue(pago.estaAprobado());
        assertEquals(72000f, pago.getMonto());
        assertTrue(pago.getDetalleMetodoPago().contains("Acreditado manualmente"));
        assertTrue(lucia.fueHabilitada());
        assertEquals("Lucía Fernández", pago.getComprobanteVigente().getNombrePagador());
        assertEquals(MetodoPago.MODO, pago.getMetodoPago().getNombre());
    }

    @Test
    @DisplayName("CU-54: al aprobar un pago con descuento cuenta su uso, igual que el pago online")
    void acreditarCuentaElDescuento() {
        Pago pendiente = pagoPendienteDeLucia();
        int usosAntes = descuento("Descuento de bienvenida").getCantidadUsada();

        pagoService.modificar(pendiente.getIdPago(), "Aprobado", 72000f, "QRTEST002", "123456789012345678901234567890", "Lucía Fernández", "40123456", null);

        assertEquals(usosAntes + 1, descuento("Descuento de bienvenida").getCantidadUsada());
    }

    @Test
    @DisplayName("CU-54: exige el nombre y el DNI del pagador para aprobar un pago de MODO")
    void exigeElPagadorDeModo() {
        Pago pendiente = pagoPendienteDeLucia();
        Integer id = pendiente.getIdPago();

        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", 72000f, "QRTEST003", "123456789012345678901234567890", "", "40123456", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", 72000f, "QRTEST003", "123456789012345678901234567890", "Lucía", "abc", null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(id, "Aprobado", 72000f, "QRTEST003", "123456789012345678901234567890", "Lucía", null, null));
        assertTrue(pendiente.estaPendiente());
    }

    @Test
    @DisplayName("CU-54: marca un pago pendiente como rechazado: solo pide el motivo, que es obligatorio, y ya no se vuelve a modificar")
    void rechazaManualmente() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());

        // sin motivo no se rechaza: es lo único que se pide
        IllegalArgumentException sinMotivo = assertThrows(IllegalArgumentException.class,
                () -> pagoService.modificar(solicitud.idPago(), "Rechazado", null, null, null, null, null, " "));
        assertTrue(sinMotivo.getMessage().contains("motivo del rechazo"));
        assertTrue(solicitud.idPago() != null && pagoRepository.findById(solicitud.idPago()).orElseThrow().estaPendiente());

        Pago pago = pagoService.modificar(solicitud.idPago(), "Rechazado", null, null, null, null, null, "No se acreditó");

        assertTrue(pago.estaRechazado());
        assertEquals("No se acreditó", pago.getMotivoRechazo());
        assertFalse(lucia.fueHabilitada());
        // rechazado, el pago es inmutable: no se puede ni acreditar ni volver a rechazar
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(solicitud.idPago(), "Aprobado", 72000f, "QRTEST004",
                "123456789012345678901234567890", "Lucía Fernández", "40123456", null));
        assertThrows(IllegalArgumentException.class,
                () -> pagoService.modificar(solicitud.idPago(), "Rechazado", null, null, null, null, null, "Otro motivo"));
    }

    @Test
    @DisplayName("CU-54: solo se modifica un pago pendiente (el aprobado y el rechazado no), con un estado válido y un monto válido")
    void validacionesDeModificarPago() {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        Pago pendiente = pagoPendienteDeLucia();
        Pago aprobado = inscripcionService.buscarConFiltros(null, MARTIN, com.app.idoneos.model.EstadoInscripcion.HABILITADA).get(0).getPagos().iterator().next();

        IllegalArgumentException estado = assertThrows(IllegalArgumentException.class,
                () -> pagoService.modificar(aprobado.getIdPago(), "Rechazado", 100f, null, "TRF-1", null, null, "Motivo"));
        assertTrue(estado.getMessage().contains("pago pendiente"));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(rechazado.getIdPago(), "Aprobado", 100f, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(pendiente.getIdPago(), "Pendiente", 100f, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(pendiente.getIdPago(), null, 100f, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(pendiente.getIdPago(), "Aprobado", null, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> pagoService.modificar(pendiente.getIdPago(), "Aprobado", -5f, null, null, null, null, null));
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

    // ----------------------------------------------------------------------
    // Buscar pago ----------
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Cierre de la inscripción ----------
    // ----------------------------------------------------------------------

    /** Hace que la inscripción a la cohorte ya haya terminado. */
    private void cerrarLaInscripcion(Inscripcion inscripcion) {
        inscripcion.getCohorte().setFechaFinInscripcion(LocalDateTime.now().minusDays(1));
        assertTrue(inscripcion.getCohorte().inscripcionCerrada());
    }

    @Test
    @DisplayName("Cierre: desde que terminó la inscripción no se inicia un pago nuevo, pero sí se continúa uno ya iniciado")
    void cerradaLaInscripcionNoSeIniciaUnPagoNuevo() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        SolicitudPago enCurso = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        cerrarLaInscripcion(lucia);

        // el pago iniciado antes del cierre se puede continuar (es la misma solicitud)
        assertEquals(enCurso.idPago(), pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno()).idPago());

        // rechazado ese pago, ya no se puede reintentar: sería un pago nuevo
        pasarela.resolver(enCurso.idSolicitud(), rechazado("INSUFFICIENT_FUNDS"));
        Pago rechazado = pagoService.consultarEstado(enCurso.idPago(), lucia.getAlumno());
        assertTrue(rechazado.estaRechazado());
        IllegalArgumentException cerrada = assertThrows(IllegalArgumentException.class,
                () -> pagoService.reintentar(rechazado.getIdPago(), lucia.getAlumno()));
        assertEquals("Error! La inscripción a esta cohorte ya cerró: no se puede iniciar un pago nuevo.", cerrada.getMessage());
    }

    @Test
    @DisplayName("Cierre: el administrador no registra ningún pago cerrada la inscripción; una con un pago parcial aprobado se cancela por falta de pago")
    void cerradaLaInscripcionNoSeRegistraUnPago() {
        Inscripcion lucia = pendienteDeLucia();
        cerrarLaInscripcion(lucia);

        IllegalArgumentException cerrada = assertThrows(IllegalArgumentException.class,
                () -> pagoService.registrarManual(lucia.getIdInscripcion(), 72000f, "TRF-000555", null));
        assertEquals("Error! La inscripción a esta cohorte ya cerró: no se puede registrar un pago.", cerrada.getMessage());

        // con un pago parcial aprobado en término, al cerrar la inscripción ya no se completa: se cancela por falta de pago
        Inscripcion antes = pendienteDeLucia();
        antes.getCohorte().setFechaFinInscripcion(LocalDateTime.now().plusDays(5));
        pagoService.registrarManual(antes.getIdInscripcion(), 1000f, "TRF-000111", null);
        cerrarLaInscripcion(antes);
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarManual(antes.getIdInscripcion(), 71000f, "TRF-000112", null));
        assertEquals(1, inscripcionService.cancelarPendientesDeInscripcionCerrada());
        assertTrue(antes.estaCancelada());
        assertTrue(antes.tienePagoAprobado()); // el pago parcial queda registrado, sin devolución
    }

    @Test
    @DisplayName("Cierre: una inscripción cancelada no se paga, ni se acredita un pago de ella")
    void unaInscripcionCanceladaNoSePaga() {
        Inscripcion lucia = pendienteDeLucia();
        cerrarLaInscripcion(lucia);
        assertEquals(1, inscripcionService.cancelarPendientesDeInscripcionCerrada());
        assertTrue(lucia.estaCancelada());

        assertEquals("Error! La inscripción fue cancelada: no se puede pagar.", assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno())).getMessage());
        assertEquals("Error! La inscripción fue cancelada: no se puede pagar.", assertThrows(IllegalArgumentException.class,
                () -> pagoService.registrarManual(lucia.getIdInscripcion(), 72000f, "TRF-000777", null)).getMessage());
    }

    @Test
    @DisplayName("Cierre: se cancela la inscripción pendiente sin pagos o con todos rechazados, y queda anotado por qué")
    void seCancelaLaPendienteSinPagoOConTodosRechazados() {
        Inscripcion lucia = pendienteDeLucia(); // tiene un pago rechazado de MODO
        Inscripcion valentina = inscribirAFinanzas(VALENTINA); // todavía no pagó nada
        assertTrue(lucia.sePuedeCancelarPorFaltaDePago() && valentina.sePuedeCancelarPorFaltaDePago());

        // con la inscripción abierta no se cancela ninguna
        assertEquals(0, inscripcionService.cancelarPendientesDeInscripcionCerrada());

        cerrarLaInscripcion(lucia);
        valentina.getCohorte().setFechaFinInscripcion(LocalDateTime.now().minusDays(1));
        assertEquals(2, inscripcionService.cancelarPendientesDeInscripcionCerrada());

        assertTrue(lucia.estaCancelada() && valentina.estaCancelada());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.CANCELADA, lucia.getNombreEstado());
        assertFalse(lucia.ocupaCupo());
        assertTrue(valentina.getObservaciones().contains("terminó la inscripción sin que se pagara"));
        // y no se vuelve a cancelar
        assertEquals(0, inscripcionService.cancelarPendientesDeInscripcionCerrada());
    }

    @Test
    @DisplayName("Cierre: nunca se cancela una inscripción con un pago en curso; con el pago en curso espera a que se resuelva. La que tiene un pago parcial aprobado sí se cancela")
    void noSeCancelaConUnPagoEnCursoOAprobado() {
        pasarela.dejarPendiente();
        Inscripcion enCurso = pendienteDeLucia();
        SolicitudPago solicitud = pagoService.iniciar(enCurso.getIdInscripcion(), enCurso.getAlumno());
        Inscripcion parcial = inscribirAFinanzas(VALENTINA);
        pagoService.registrarManual(parcial.getIdInscripcion(), 1000f, "TRF-000111", null); // aprobado, pero no cubre el precio
        cerrarLaInscripcion(enCurso);
        parcial.getCohorte().setFechaFinInscripcion(LocalDateTime.now().minusDays(1));

        assertTrue(enCurso.estaConPagoEnCurso());
        assertFalse(enCurso.sePuedeCancelarPorFaltaDePago());
        assertTrue(parcial.sePuedeCancelarPorFaltaDePago());
        // la del pago parcial se cancela (el pago queda registrado, sin devolución). La del pago en curso espera
        assertEquals(1, inscripcionService.cancelarPendientesDeInscripcionCerrada());
        assertTrue(enCurso.estaConPagoEnCurso() && parcial.estaCancelada() && parcial.tienePagoAprobado());

        // cuando el pago en curso se rechaza, la inscripción vuelve a pendiente y en la corrida siguiente se cancela
        pasarela.resolver(solicitud.idSolicitud(), rechazado("INSUFFICIENT_FUNDS"));
        pagoService.consultarEstado(solicitud.idPago(), enCurso.getAlumno());
        assertEquals(1, inscripcionService.cancelarPendientesDeInscripcionCerrada());
        assertTrue(enCurso.estaCancelada());
        assertTrue(parcial.estaCancelada());
    }

    @Test
    @DisplayName("Cupo: la inscripción con un pago en curso reserva su lugar y lo libera cuando el pago se rechaza")
    void elPagoEnCursoReservaElCupo() {
        pasarela.dejarPendiente();
        Inscripcion lucia = pendienteDeLucia();
        Inscripcion otra = inscribirAFinanzas(VALENTINA); // se inscribe antes de que se complete el cupo
        int ocupadosAntes = lucia.getCohorte().getCuposOcupados();

        SolicitudPago solicitud = pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno());
        assertEquals(ocupadosAntes + 1, lucia.getCohorte().getCuposOcupados());

        // con el cupo completo por esa reserva, otra inscripción no puede pagar, pero la que reserva puede seguir con su pago
        lucia.getCohorte().setCupoMaximo(lucia.getCohorte().getCuposOcupados());
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> pagoService.iniciar(otra.getIdInscripcion(), otra.getAlumno())).getMessage().contains("cupo máximo"));
        assertEquals(solicitud.idPago(), pagoService.iniciar(lucia.getIdInscripcion(), lucia.getAlumno()).idPago());

        pasarela.resolver(solicitud.idSolicitud(), rechazado("INSUFFICIENT_FUNDS"));
        pagoService.consultarEstado(solicitud.idPago(), lucia.getAlumno());
        assertEquals(ocupadosAntes, lucia.getCohorte().getCuposOcupados());
        assertNotNull(pagoService.iniciar(otra.getIdInscripcion(), otra.getAlumno()).idSolicitud()); // ahora el lugar está libre
    }
}
