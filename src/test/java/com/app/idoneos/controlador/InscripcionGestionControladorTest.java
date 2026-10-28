package com.app.idoneos.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;

/**
 * Pruebas de las pantallas y rutas de las inscripciones (CU-44 a CU-49): quién puede usar cada una y qué responde.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InscripcionGestionControladorTest {

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
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    private Cohorte cohorteAbierta(String curso) {
        Integer idPrograma = programaServicio.buscarPorCurso(cursoServicio.buscarPorNombre(curso).orElseThrow())
                .get(0).getIdPrograma();
        return cohorteServicio.buscarConFiltros(idPrograma, null, "Abierta", null, null, "recientes", null).get(0);
    }

    private Inscripcion inscripcion(String correo, String estadoAcceso) {
        return inscripcionServicio.buscarConFiltros(null, correo, estadoAcceso).get(0);
    }

    // ---------- CU-44 Buscar inscripción ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el administrador ve el listado con sus filtros, estados, certificados y ventanas")
    void buscarComoAdministrador() throws Exception {
        mockMvc.perform(get("/inscripcion/buscar")).andExpect(status().isOk())
                .andExpect(view().name("pages/inscripcion/buscarAdministrador"))
                .andExpect(model().attribute("menuActivo", "inscripciones"))
                .andExpect(model().attribute("inscripciones", org.hamcrest.Matchers.hasSize(7)))
                .andExpect(content().string(containsString("data-filtro-front")))
                .andExpect(content().string(containsString("Pendiente de pago")))
                .andExpect(content().string(containsString("Programada")))
                .andExpect(content().string(containsString("Vencida")))
                .andExpect(content().string(containsString("CERT-2026-000001")))
                .andExpect(content().string(containsString("modalNuevaInscripcion")))
                .andExpect(content().string(containsString("modalEditarInscripcion")))
                .andExpect(content().string(containsString("modalCertificado")))
                .andExpect(content().string(containsString("modalBajaInscripcion")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el menú lateral del administrador tiene el enlace a Inscripciones")
    void menuConInscripciones() throws Exception {
        mockMvc.perform(get("/inscripcion/buscar")).andExpect(content().string(containsString("/inscripcion/buscar")))
                .andExpect(content().string(containsString("Inscripciones")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el alumno no accede al listado de todas las inscripciones")
    void alumnoNoVeElListadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/inscripcion/buscar")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el docente no accede al listado de inscripciones")
    void docenteNoVeElListado() throws Exception {
        mockMvc.perform(get("/inscripcion/buscar")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el alumno ve el estado de acceso de cada inscripción y no puede entrar a las que no están vigentes")
    void misCursosConEstadosDeAcceso() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Pendiente de pago")))
                .andExpect(content().string(containsString("Comienza el")))
                .andExpect(content().string(containsString("data-f-acceso")))
                .andExpect(content().string(containsString("modalBajaInscripcion")))
                .andExpect(content().string(not(containsString("Acceder al curso"))));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el alumno con acceso vigente ve el botón para acceder al curso")
    void misCursosConAccesoVigente() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Acceder al curso")));
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-44: el alumno con el acceso vencido ve su inscripción bloqueada")
    void accesoVencido() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Acceso vencido")));
    }

    // ---------- CU-45 Inscribir al curso ----------

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: el alumno se inscribe a un curso gratuito y se lo lleva a Mis cursos")
    void inscribirseGratis() throws Exception {
        Cohorte cohorte = cohorteAbierta("Macroeconomía de Coyuntura");

        mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte()))
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Ya podés acceder")))
                .andExpect(jsonPath("$.destino").value("/inscripcion/misCursos"));

        assertEquals(1, cohorte.getCantidadInscriptos());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: al inscribirse a un curso con costo se avisa que falta el pago")
    void inscribirseConCosto() throws Exception {
        Cohorte cohorte = cohorteAbierta("Finanzas Personales e Inversión");

        mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte()))
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("completar el pago")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: sin aceptar los términos y condiciones no se inscribe")
    void inscribirseSinTerminos() throws Exception {
        Cohorte cohorte = cohorteAbierta("Macroeconomía de Coyuntura");

        mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("términos y condiciones")));

        assertEquals(0, cohorte.getCantidadInscriptos());
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: no se inscribe dos veces a la misma cohorte")
    void inscribirseDosVeces() throws Exception {
        Cohorte cohorte = cohorteAbierta("Finanzas Personales e Inversión");

        mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte()))
                .param("terminos", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("ya tiene una inscripción vigente")));
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: solo los alumnos se inscriben")
    void soloLosAlumnosSeInscriben() throws Exception {
        mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", "1").param("terminos", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: el administrador tampoco se inscribe: registra la inscripción de un alumno (CU-46)")
    void elAdministradorNoSeInscribe() throws Exception {
        mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", "1").param("terminos", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CU-45: en la ficha, el visitante ve un enlace para ingresar y no el botón de inscripción")
    void fichaParaElVisitante() throws Exception {
        Integer idCurso = cursoServicio.buscarPorNombre("Macroeconomía de Coyuntura").orElseThrow().getIdCurso();

        mockMvc.perform(get("/catalogo/ficha/" + idCurso)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresá para inscribirte")))
                .andExpect(content().string(not(containsString("formInscripcion"))));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: en la ficha, el alumno ve el botón y la ventana para inscribirse con los términos")
    void fichaParaElAlumno() throws Exception {
        Integer idCurso = cursoServicio.buscarPorNombre("Finanzas Personales e Inversión").orElseThrow().getIdCurso();

        mockMvc.perform(get("/catalogo/ficha/" + idCurso)).andExpect(status().isOk())
                .andExpect(content().string(containsString("formInscripcion")))
                .andExpect(content().string(containsString("/inscripcion/inscribir")))
                .andExpect(content().string(containsString("términos y condiciones")))
                .andExpect(content().string(containsString("El curso cuesta")));
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-45: en la ficha, quien no es alumno ve que solo los alumnos se inscriben")
    void fichaParaElDocente() throws Exception {
        Integer idCurso = cursoServicio.buscarPorNombre("Macroeconomía de Coyuntura").orElseThrow().getIdCurso();

        mockMvc.perform(get("/catalogo/ficha/" + idCurso)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Solo los alumnos pueden inscribirse")))
                .andExpect(content().string(not(containsString("formInscripcion"))));
    }

    // ---------- CU-46 Registrar inscripción ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-46: el administrador registra la inscripción de un alumno")
    void registrar() throws Exception {
        Cohorte cohorte = cohorteAbierta("Finanzas Personales e Inversión");
        Integer idAlumno = inscripcion(VALENTINA, "Vigente").getAlumno().getIdAlumno();

        mockMvc.perform(post("/inscripcion/registrar").param("alumnoId", idAlumno.toString())
                .param("cohorteId", String.valueOf(cohorte.getIdCohorte())).param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Queda pendiente de pago")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-46: pide el alumno y la confirmación de los términos")
    void registrarIncompleto() throws Exception {
        Cohorte cohorte = cohorteAbierta("Finanzas Personales e Inversión");

        mockMvc.perform(post("/inscripcion/registrar").param("cohorteId", String.valueOf(cohorte.getIdCohorte())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("alumno")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-46: el alumno no registra inscripciones de otros")
    void alumnoNoRegistra() throws Exception {
        mockMvc.perform(post("/inscripcion/registrar").param("alumnoId", "1").param("cohorteId", "1")
                .param("terminos", "true")).andExpect(status().isForbidden());
    }

    // ---------- CU-47 Modificar inscripción ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-47: el administrador modifica una inscripción")
    void modificar() throws Exception {
        Inscripcion pendiente = inscripcion(LUCIA, "Pendiente de pago");

        mockMvc.perform(post("/inscripcion/modificar/" + pendiente.getIdInscripcion())
                .param("observaciones", "Pago confirmado por transferencia"))
                .andExpect(status().isOk());
        assertEquals("Pago confirmado por transferencia", pendiente.getObservaciones());

        // habilitarla a mano sin un pago aprobado se rechaza
        mockMvc.perform(post("/inscripcion/modificar/" + pendiente.getIdInscripcion()).param("habilitada", "true"))
                .andExpect(status().isBadRequest());
        assertTrue(!pendiente.estaHabilitada());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-47: rechaza un vencimiento anterior al de la duración del cronograma")
    void modificarConVencimientoInvalido() throws Exception {
        Inscripcion vigente = inscripcion(VALENTINA, "Vigente");

        mockMvc.perform(post("/inscripcion/modificar/" + vigente.getIdInscripcion())
                .param("vencimiento", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("duración del cronograma")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-47: el alumno no modifica inscripciones")
    void alumnoNoModifica() throws Exception {
        mockMvc.perform(post("/inscripcion/modificar/1").param("vencimiento", LocalDate.now().plusYears(1).toString()))
                .andExpect(status().isForbidden());
    }

    // ---------- CU-48 Corregir datos personales del certificado ----------

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-48: el administrador corrige el nombre y el DNI de un certificado")
    void corregirCertificado() throws Exception {
        var certificado = inscripcion(MARTIN, "Vencida").getCertificadoVigente();

        mockMvc.perform(post("/certificado/corregir/" + certificado.getIdCertificado())
                .param("nombre", "Martín Gómez Ruiz").param("dni", "41234568"))
                .andExpect(status().isOk());

        assertEquals("Martín Gómez Ruiz", certificado.getNombreAlumno());
        assertEquals("41234568", certificado.getDniAlumno());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-48: rechaza un DNI inválido")
    void corregirCertificadoConDniInvalido() throws Exception {
        var certificado = inscripcion(MARTIN, "Vencida").getCertificadoVigente();

        mockMvc.perform(post("/certificado/corregir/" + certificado.getIdCertificado())
                .param("nombre", "Martín Gómez").param("dni", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("DNI")));
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-48: el alumno no corrige certificados")
    void alumnoNoCorrigeCertificados() throws Exception {
        mockMvc.perform(post("/certificado/corregir/1").param("nombre", "X").param("dni", "12345678"))
                .andExpect(status().isForbidden());
    }

    // ---------- CU-49 Dar de baja inscripción ----------

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-49: el alumno se da de baja de su inscripción, con motivo")
    void alumnoSeDaDeBaja() throws Exception {
        Inscripcion vigente = inscripcion(VALENTINA, "Vigente");

        mockMvc.perform(post("/inscripcion/darDeBaja/" + vigente.getIdInscripcion()).param("motivo", "No tengo tiempo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("dada de baja")));

        assertTrue(vigente.esInactivo());
        assertTrue(vigente.getObservaciones().contains("No tengo tiempo"));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-49: el alumno no da de baja la inscripción de otro")
    void alumnoNoDaDeBajaLaAjena() throws Exception {
        Inscripcion deMartin = inscripcion(MARTIN, "Vigente");

        mockMvc.perform(post("/inscripcion/darDeBaja/" + deMartin.getIdInscripcion()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("no te pertenece")));

        assertFalse(deMartin.esInactivo());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-49: el administrador da de baja cualquier inscripción")
    void administradorDaDeBaja() throws Exception {
        Inscripcion deMartin = inscripcion(MARTIN, "Vigente");

        mockMvc.perform(post("/inscripcion/darDeBaja/" + deMartin.getIdInscripcion()).param("motivo", "Fraude"))
                .andExpect(status().isOk());

        assertTrue(deMartin.esInactivo());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-49: las pantallas del foro muestran de forma anónima la consulta de quien se dio de baja")
    void foroAnonimo() throws Exception {
        var consulta = consultaForoRepositorio.findAll().stream()
                .filter(c -> c.getInscripcion().getAlumno().getUsuario().getCorreo().equals(VALENTINA)).findFirst().orElseThrow();
        inscripcionServicio.darDeBajaInscripcion(consulta.getInscripcion().getIdInscripcion(), null, null);

        mockMvc.perform(get("/foro/ver/" + consulta.getTema().getIdTema())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Alumno anónimo")))
                .andExpect(content().string(not(containsString("Valentina Ruiz"))));
        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Alumno anónimo")));
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-49: el docente no da de baja inscripciones")
    void docenteNoDaDeBaja() throws Exception {
        mockMvc.perform(post("/inscripcion/darDeBaja/1")).andExpect(status().isForbidden());
    }
}
