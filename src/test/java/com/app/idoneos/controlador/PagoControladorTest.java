package com.app.idoneos.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;
import com.app.idoneos.servicio.Pago.PagoServicioImpl;
import com.app.idoneos.servicio.Pago.PasarelaSimulada;

/**
 * Pruebas de las pantallas y rutas de los pagos (CU-50 a CU-54): quién puede usar cada una y qué responde, con la
 * plataforma de pagos simulada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PagoControladorTest {

    private static final String ADMIN = "admin@idoneos.online";
    private static final String DOCENTE = "fausto.spotorno@idoneos.online";
    private static final String LUCIA = "lucia.fernandez@correo.com";
    private static final String MARTIN = "martin.gomez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String SERVICIO = "usuarioDetallesServicio";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    @Autowired
    private PagoServicioImpl pagoServicio;

    @Autowired
    private PasarelaSimulada pasarela;

    @BeforeEach
    @AfterEach
    void reiniciarPasarela() {
        pasarela.reiniciar();
    }

    private Inscripcion pendienteDeLucia() {
        return inscripcionServicio.buscarConFiltros(null, LUCIA, Inscripcion.ACCESO_PENDIENTE_DE_PAGO).get(0);
    }

    private Pago pagoAprobadoDe(String correo) {
        return inscripcionServicio.buscarConFiltros(null, correo, Inscripcion.ACCESO_VIGENTE).get(0).getPagoAprobado();
    }

    // ---------- CU-50 Buscar pago ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el administrador ve todos los pagos con sus filtros, comprobantes y ventanas")
    void buscarComoAdministrador() throws Exception {
        mockMvc.perform(get("/pago/buscar")).andExpect(status().isOk())
                .andExpect(view().name("pages/pago/buscarAdministrador"))
                .andExpect(model().attribute("menuActivo", "pagos"))
                .andExpect(model().attribute("pagos", org.hamcrest.Matchers.hasSize(7)))
                .andExpect(content().string(containsString("data-filtro-front")))
                .andExpect(content().string(containsString("COMP-")))
                .andExpect(content().string(containsString("INSUFFICIENT_FUNDS")))
                .andExpect(content().string(containsString("modalRegistrarPago")))
                .andExpect(content().string(containsString("modalModificarPago")))
                .andExpect(content().string(containsString("0000003100012345678901"))) // el CBU de la institución
                .andExpect(content().string(containsString("/pago/comprobante/")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-53: se llega a registrar el pago desde la inscripción pendiente y se abre la ventana con ella elegida")
    void registrarParaUnaInscripcion() throws Exception {
        Inscripcion lucia = pendienteDeLucia();

        mockMvc.perform(get("/pago/buscar").param("registrarPara", String.valueOf(lucia.getIdInscripcion())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-abrir-modal=\"modalRegistrarPago\"")))
                .andExpect(content().string(containsString("data-monto=\"72000.00\" selected=\"selected\""))); // elegida y con el monto sugerido (con su descuento)
        mockMvc.perform(get("/inscripcion/buscar")).andExpect(content().string(containsString("Registrar pago")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el alumno no accede al listado de todos los pagos")
    void alumnoNoVeElListadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/pago/buscar")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el docente no accede a los pagos")
    void docenteNoVeLosPagos() throws Exception {
        mockMvc.perform(get("/pago/buscar")).andExpect(status().isForbidden());
        mockMvc.perform(get("/pago/misPagos")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el administrador no usa la pantalla de pagos del alumno")
    void administradorNoUsaMisPagos() throws Exception {
        mockMvc.perform(get("/pago/misPagos")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50 y CU-52: el alumno ve sus pagos y puede reintentar el rechazado y bajar el comprobante del aprobado")
    void misPagosConReintento() throws Exception {
        mockMvc.perform(get("/pago/misPagos")).andExpect(status().isOk())
                .andExpect(view().name("pages/pago/misPagos"))
                .andExpect(model().attribute("pagos", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(content().string(containsString("Reintentar")))
                .andExpect(content().string(containsString("data-pagar")))
                .andExpect(content().string(containsString("INSUFFICIENT_FUNDS")))
                .andExpect(content().string(containsString("/pago/comprobante/")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el alumno con todo pago no ve el botón para reintentar")
    void misPagosSinReintento() throws Exception {
        mockMvc.perform(get("/pago/misPagos")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("data-pagar"))))
                .andExpect(content().string(containsString("Comprobante")));
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: en Mis cursos, la inscripción pendiente de pago ofrece pagar con el monto que le corresponde")
    void misCursosOfrecePagar() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(containsString("data-pagar")))
                .andExpect(content().string(containsString("Pagar $ 72.000")))
                .andExpect(content().string(containsString("/pago/misPagos")))
                .andExpect(content().string(containsString("/js/pago.js")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: sin inscripciones pendientes de pago no se ofrece pagar")
    void misCursosSinPendientes() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("data-pagar"))));
    }

    // ---------- CU-51 Realizar pago / CU-52 Reintentar pago ----------

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: el alumno inicia el pago y recibe lo necesario para mostrar el pago")
    void pagar() throws Exception {
        Inscripcion lucia = pendienteDeLucia();

        mockMvc.perform(post("/pago/pagar/" + lucia.getIdInscripcion())).andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").isNumber())
                .andExpect(jsonPath("$.idSolicitud").value(org.hamcrest.Matchers.startsWith("SIM-")))
                .andExpect(jsonPath("$.qr").isNotEmpty())
                .andExpect(jsonPath("$.simulado").value(true))
                .andExpect(jsonPath("$.aprobado").value(false))
                .andExpect(jsonPath("$.plazoMinutos").value(10));
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: el alumno no paga la inscripción de otro")
    void noPagaLaAjena() throws Exception {
        mockMvc.perform(post("/pago/pagar/" + pendienteDeLucia().getIdInscripcion()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("No tenés una inscripción pendiente de pago")));
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: no se paga una inscripción que ya está paga")
    void noPagaDosVeces() throws Exception {
        Inscripcion pagada = inscripcionServicio.buscarConFiltros(null, MARTIN, Inscripcion.ACCESO_VIGENTE).get(0);

        mockMvc.perform(post("/pago/pagar/" + pagada.getIdInscripcion())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("ya está aprobado")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: el administrador no paga como alumno")
    void administradorNoPaga() throws Exception {
        mockMvc.perform(post("/pago/pagar/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-52: el alumno reintenta su pago rechazado, que vuelve a quedar pendiente con el mismo registro")
    void reintentar() throws Exception {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();

        mockMvc.perform(post("/pago/reintentar/" + rechazado.getIdPago())).andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(rechazado.getIdPago()));

        assertTrue(rechazado.estaPendiente());
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-52: no se reintenta un pago ajeno ni uno que no está rechazado")
    void reintentarInvalido() throws Exception {
        Pago rechazadoDeLucia = pendienteDeLucia().getPagoSinAprobar();
        mockMvc.perform(post("/pago/reintentar/" + rechazadoDeLucia.getIdPago())).andExpect(status().isBadRequest());

        Pago aprobado = pagoAprobadoDe(MARTIN);
        mockMvc.perform(post("/pago/reintentar/" + aprobado.getIdPago())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("no se encuentra rechazado")));
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: el estado del pago se concilia con MODO y el navegador lo consulta hasta que se resuelve")
    void estadoDelPago() throws Exception {
        pasarela.setEsperarConfirmacion(true);
        Inscripcion lucia = pendienteDeLucia();
        int idPago = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno()).idPago();

        mockMvc.perform(get("/pago/estado/" + idPago)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Pendiente"));

        pasarela.setEsperarConfirmacion(false);
        mockMvc.perform(get("/pago/estado/" + idPago)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Aprobado"));
        assertTrue(lucia.estaHabilitada());
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: el alumno no consulta el estado del pago de otro")
    void estadoDePagoAjeno() throws Exception {
        Pago deLucia = pendienteDeLucia().getPagoSinAprobar();

        mockMvc.perform(get("/pago/estado/" + deLucia.getIdPago())).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-51: quien no es alumno ni administrador no consulta pagos")
    void estadoParaElDocente() throws Exception {
        Pago deLucia = pendienteDeLucia().getPagoSinAprobar();

        mockMvc.perform(get("/pago/estado/" + deLucia.getIdPago())).andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("CU-51: el aviso de MODO (webhook) no requiere sesión, responde 200 y concilia el pago")
    void webhook() throws Exception {
        Inscripcion lucia = pendienteDeLucia();
        var solicitud = pagoServicio.iniciarPago(lucia.getIdInscripcion(), lucia.getAlumno());

        mockMvc.perform(post("/pago/webhook").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + solicitud.idSolicitud() + "\",\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recibido").value(true));

        assertTrue(lucia.getPagoAprobado() != null);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("CU-51: un aviso falso o vacío no aprueba nada y se responde igual con 200")
    void webhookInvalido() throws Exception {
        Inscripcion lucia = pendienteDeLucia();

        mockMvc.perform(post("/pago/webhook").contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"inventado\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/pago/webhook").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        mockMvc.perform(post("/pago/webhook").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        assertTrue(lucia.getPagoAprobado() == null);
    }

    // ---------- CU-53 Registrar pago ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-53: el administrador registra el pago por transferencia")
    void registrarPago() throws Exception {
        Inscripcion lucia = pendienteDeLucia();

        mockMvc.perform(post("/pago/registrar").param("idInscripcion", String.valueOf(lucia.getIdInscripcion()))
                .param("monto", "72000").param("referencia", "TRF-555111").param("observaciones", "Banco Nación"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Lucía Fernández")));

        assertTrue(lucia.estaHabilitada());
        assertEquals("TRF-555111", lucia.getPagoAprobado().getCodigoReferencia());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-53: pide la inscripción, el monto y el comprobante")
    void registrarPagoIncompleto() throws Exception {
        mockMvc.perform(post("/pago/registrar")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("inscripción")));
        mockMvc.perform(post("/pago/registrar").param("idInscripcion", String.valueOf(pendienteDeLucia().getIdInscripcion())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("monto y el comprobante")));
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-53: el alumno no registra pagos")
    void alumnoNoRegistraPagos() throws Exception {
        mockMvc.perform(post("/pago/registrar").param("idInscripcion", "1").param("monto", "100").param("referencia", "X"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-46: al registrar una inscripción pendiente de pago se pasa a registrar su pago")
    void registrarInscripcionLlevaARegistrarElPago() throws Exception {
        Inscripcion lucia = pendienteDeLucia();
        Integer idAlumno = inscripcionServicio.buscarConFiltros(null, VALENTINA, null).get(0).getAlumno().getIdAlumno();
        Integer idCohorte = lucia.getCohorte().getIdCohorte(); // Finanzas Personales, abierta y con costo

        mockMvc.perform(post("/inscripcion/registrar").param("alumnoId", idAlumno.toString())
                .param("cohorteId", idCohorte.toString()).param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destino").value(containsString("/pago/buscar?registrarPara=")));
    }

    // ---------- CU-54 Modificar pago ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-54: el administrador acredita un pago de MODO que no se confirmó")
    void modificarPago() throws Exception {
        Inscripcion lucia = pendienteDeLucia();
        Pago rechazado = lucia.getPagoSinAprobar();

        mockMvc.perform(post("/pago/modificar/" + rechazado.getIdPago()).param("estado", "Aprobado").param("monto", "72000")
                .param("nombrePagador", "Lucía Fernández").param("dniPagador", "40123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Pago acreditado")));

        assertTrue(rechazado.estaAprobado());
        assertTrue(lucia.estaHabilitada());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-54: rechaza datos inválidos y los pagos que ya están aprobados")
    void modificarPagoInvalido() throws Exception {
        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        mockMvc.perform(post("/pago/modificar/" + rechazado.getIdPago()).param("estado", "Aprobado").param("monto", "72000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("nombre y el DNI")));

        Pago aprobado = pagoAprobadoDe(MARTIN);
        mockMvc.perform(post("/pago/modificar/" + aprobado.getIdPago()).param("estado", "Rechazado").param("monto", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("pendiente o rechazado")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-54: el alumno no modifica pagos")
    void alumnoNoModificaPagos() throws Exception {
        mockMvc.perform(post("/pago/modificar/1").param("estado", "Aprobado").param("monto", "100"))
                .andExpect(status().isForbidden());
    }

    // ---------- Comprobante ----------

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el alumno descarga el comprobante de su pago aprobado, en PDF")
    void descargaElComprobante() throws Exception {
        Pago aprobado = pagoAprobadoDe(VALENTINA);

        byte[] pdf = mockMvc.perform(get("/pago/comprobante/" + aprobado.getIdPago())).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Disposition",
                        containsString(aprobado.getComprobanteVigente().getNumero())))
                .andReturn().getResponse().getContentAsByteArray();

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el alumno no descarga el comprobante de otro, ni de un pago sin aprobar")
    void comprobanteAjenoOSinAprobar() throws Exception {
        Pago deValentina = pagoAprobadoDe(VALENTINA);
        mockMvc.perform(get("/pago/comprobante/" + deValentina.getIdPago())).andExpect(status().isNotFound());

        Pago rechazado = pendienteDeLucia().getPagoSinAprobar();
        mockMvc.perform(get("/pago/comprobante/" + rechazado.getIdPago())).andExpect(status().isNotFound());
        mockMvc.perform(get("/pago/comprobante/99999")).andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-50: el administrador descarga el comprobante de cualquier pago aprobado")
    void administradorDescargaCualquierComprobante() throws Exception {
        Pago aprobado = pagoAprobadoDe(VALENTINA);

        mockMvc.perform(get("/pago/comprobante/" + aprobado.getIdPago())).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("CU-50: sin sesión no se accede a los pagos ni a los comprobantes")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/pago/misPagos")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/pago/buscar")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/pago/comprobante/1")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/pago/estado/1")).andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/pago/pagar/1")).andExpect(status().is3xxRedirection());
    }
}
