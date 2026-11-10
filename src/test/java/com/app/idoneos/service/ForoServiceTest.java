package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.RespuestaForo;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.consultaforo.ConsultaForoService;
import com.app.idoneos.service.respuestaforo.RespuestaForoService;

/** Pruebas del foro con los datos de la semilla. */
@SpringBootTest
@Transactional
class ForoServiceTest {

    @Autowired
    private ConsultaForoService consultaService;

    @Autowired
    private RespuestaForoService respuestaService;

    @Autowired
    private ConsultaForoRepository consultaRepository;

    @Autowired
    private RespuestaForoRepository respuestaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    private Alumno alumno(String correo) {
        return alumnoRepository.findAll().stream().filter(a -> a.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Tema tema(String curso, String titulo) {
        return temaRepository.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private Tema primera() {
        return tema("Mercado de Capitales Argentino", "Introducción al mercado de capitales");
    }

    private Tema segunda() {
        return tema("Mercado de Capitales Argentino", "Renta fija y bonos");
    }

    private Tema tercera() {
        return tema("Mercado de Capitales Argentino", "Renta variable y opciones");
    }

    private Alumno valentina() {
        return alumno("valentina.ruiz@correo.com");
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private String mensaje(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    // ----------------------------------------------------------------------
    // Buscar consulta
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-35: se buscan las consultas por texto y por rango de fechas, sin distinguir mayúsculas ni tildes")
    void busca() {
        assertEquals(1, consultaService.buscarConFiltros(primera(), null, null, null).size());
        assertEquals(1, consultaService.buscarConFiltros(primera(), "MATRICULA", null, null).size());
        assertTrue(consultaService.buscarConFiltros(primera(), "duration", null, null).isEmpty());

        LocalDate hoy = LocalDate.now();
        // la consulta del tema 1 es de hace 3 días
        assertEquals(1, consultaService.buscarConFiltros(primera(), null, hoy.minusDays(4), hoy.minusDays(2)).size());
        assertTrue(consultaService.buscarConFiltros(primera(), null, hoy.minusDays(2), null).isEmpty());
        assertTrue(consultaService.buscarConFiltros(primera(), null, null, hoy.minusDays(4)).isEmpty());
    }

    @Test
    @DisplayName("CU-35: las consultas se listan con el último mensaje más reciente primero")
    void ordenaPorUltimoMensaje() {
        ConsultaForo nueva = consultaService.registrar(valentina(), primera().getIdTema(), "Otra consulta");

        List<ConsultaForo> consultas = consultaService.buscarConFiltros(primera(), null, null, null);

        assertEquals(nueva.getIdConsulta(), consultas.get(0).getIdConsulta());
        assertEquals(2, consultas.size());
    }

    // ----------------------------------------------------------------------
    // Registrar consulta
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-36: el alumno con el tema habilitado registra una consulta")
    void registraConsulta() {
        ConsultaForo consulta = consultaService.registrar(valentina(), segunda().getIdTema(),
                "  ¿Qué es el cupón corrido?  ");

        assertTrue(consulta.getIdConsulta() > 0);
        assertEquals("¿Qué es el cupón corrido?", consulta.getTexto());
        assertEquals("Valentina Ruiz", consulta.getAutor());
        assertTrue(consulta.estaSinResponder());
        assertEquals(2, segunda().getCantidadConsultasForo());
    }

    @Test
    @DisplayName("CU-36: falta el texto, supera el máximo o el alumno no tiene el tema habilitado")
    void validaAlRegistrar() {
        int tema = segunda().getIdTema();
        assertTrue(mensaje(() -> consultaService.registrar(valentina(), tema, " "))
                .contains("Debe completar el texto de la consulta."));
        assertTrue(mensaje(() -> consultaService.registrar(valentina(), tema, "x".repeat(501)))
                .contains("Máximo: 500 caracteres"));
        assertNotNull(consultaService.registrar(valentina(), tema, "x".repeat(500)));

        // tema bloqueado, dictado sin comenzar, sin inscripción, sin tema y sin ser alumno
        assertTrue(mensaje(() -> consultaService.registrar(valentina(), tercera().getIdTema(), "Hola"))
                .contains("No tenés una inscripción vigente"));
        assertTrue(mensaje(() -> consultaService.registrar(alumno("martin.gomez@correo.com"), tema, "Hola"))
                .contains("No tenés una inscripción vigente"));
        assertTrue(mensaje(() -> consultaService.registrar(alumno("tomas.herrera@correo.com"), tema, "Hola"))
                .contains("No tenés una inscripción vigente"));
        assertTrue(mensaje(() -> consultaService.registrar(valentina(), null, "Hola"))
                .contains("El tema no se encuentra activa"));
        assertTrue(mensaje(() -> consultaService.registrar(null, tema, "Hola")).contains("rol de alumno"));
    }

    // ----------------------------------------------------------------------
    // Modificar consulta
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-37: el alumno modifica su consulta dentro del plazo")
    void modificaConsulta() {
        ConsultaForo consulta = consultaService.registrar(valentina(), segunda().getIdTema(), "Texto");

        ConsultaForo modificada = consultaService.modificar(consulta.getIdConsulta(), valentina(), "Texto nuevo");

        assertEquals("Texto nuevo", modificada.getTexto());
        assertNotNull(modificada.getUltimaModificacion());
        assertTrue(modificada.puedeModificar(valentina(), 15));
    }

    @Test
    @DisplayName("CU-37: no se modifica una consulta ajena, vencida, vacía o dada de baja")
    void validaAlModificarConsulta() {
        ConsultaForo consulta = consultaService.registrar(valentina(), segunda().getIdTema(), "Texto");
        int id = consulta.getIdConsulta();

        assertTrue(mensaje(() -> consultaService.modificar(id, alumno("tomas.herrera@correo.com"), "Otro"))
                .contains("tus propias consultas"));
        assertTrue(mensaje(() -> consultaService.modificar(id, valentina(), " "))
                .contains("Debe completar el texto de la consulta."));

        consulta.setFechaCreacion(LocalDateTime.now().minusMinutes(16));
        assertTrue(mensaje(() -> consultaService.modificar(id, valentina(), "Tarde"))
                .contains("pasó el plazo de edición de 15 minutos"));
        assertFalse(consulta.puedeModificar(valentina(), 15));

        consultaService.darDeBaja(id);
        assertTrue(mensaje(() -> consultaService.modificar(id, valentina(), "Baja"))
                .contains("no se encuentra activa"));
    }

    // ----------------------------------------------------------------------
    // Dar de baja consulta
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-38: dar de baja una consulta también da de baja sus respuestas")
    void daDeBajaConsultaConRespuestas() {
        ConsultaForo consulta = primera().getConsultasForoOrdenadas().get(0);
        int idRespuesta = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        consultaService.darDeBaja(consulta.getIdConsulta());

        assertTrue(consultaRepository.findById(consulta.getIdConsulta()).orElseThrow().getBaja());
        assertTrue(respuestaRepository.findById(idRespuesta).orElseThrow().getBaja());
        assertEquals(0, primera().getCantidadConsultasForo());
        assertTrue(mensaje(() -> consultaService.darDeBaja(consulta.getIdConsulta()))
                .contains("no se encuentra activa"));
    }

    // ----------------------------------------------------------------------
    // Respuestas
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-40: el docente del curso responde una consulta y esta deja de estar sin responder")
    void respondeConsulta() {
        ConsultaForo consulta = segunda().getConsultasForoOrdenadas().get(0);
        assertTrue(consulta.estaSinResponder());

        RespuestaForo respuesta = respuestaService.registrar(fausto(), consulta.getIdConsulta(),
                "  Es el interés devengado desde el último pago.  ");

        assertEquals("Es el interés devengado desde el último pago.", respuesta.getTexto());
        assertEquals("Fausto Spotorno", respuesta.getAutor());
        assertFalse(consulta.estaSinResponder());
        assertEquals(0, segunda().getCantidadConsultasSinResponder());
    }

    @Test
    @DisplayName("CU-40: falta el texto, supera el máximo, la consulta no está activa o el docente no participa")
    void validaAlResponder() {
        ConsultaForo consulta = segunda().getConsultasForoOrdenadas().get(0);
        int id = consulta.getIdConsulta();

        assertTrue(mensaje(() -> respuestaService.registrar(fausto(), id, ""))
                .contains("Debe completar el texto de la respuesta."));
        assertTrue(mensaje(() -> respuestaService.registrar(fausto(), id, "x".repeat(501)))
                .contains("Máximo: 500 caracteres"));
        assertTrue(mensaje(() -> respuestaService.registrar(null, id, "Hola")).contains("rol de docente"));
        assertTrue(mensaje(() -> respuestaService.registrar(fausto(), 999999, "Hola"))
                .contains("La consulta no se encuentra activa"));

        // una consulta del curso "Macroeconomía de Coyuntura", donde Sebastián Bordato no participa
        ConsultaForo ajena = consultaRepository.save(new ConsultaForo(
                tema("Macroeconomía de Coyuntura", "Inflación y precios"), consulta.getInscripcion(), "Consulta"));
        assertTrue(mensaje(() -> respuestaService.registrar(docente("sebastian.bordato@idoneos.online"),
                ajena.getIdConsulta(), "Hola")).contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-41: el docente modifica su respuesta dentro del plazo, pero no la ajena ni la vencida")
    void modificaRespuesta() {
        ConsultaForo consulta = segunda().getConsultasForoOrdenadas().get(0);
        RespuestaForo respuesta = respuestaService.registrar(fausto(), consulta.getIdConsulta(), "Texto");
        int id = respuesta.getIdRespuesta();

        assertEquals("Texto nuevo", respuestaService.modificar(id, fausto(), "Texto nuevo").getTexto());
        // Sebastián participa en el curso (ayudante), pero la respuesta no es suya
        assertTrue(mensaje(() -> respuestaService.modificar(id, docente("sebastian.bordato@idoneos.online"),
                "Otro")).contains("tus propias respuestas"));
        assertTrue(mensaje(() -> respuestaService.modificar(id, fausto(), " "))
                .contains("Debe completar el texto de la respuesta."));

        respuesta.setFechaCreacion(LocalDateTime.now().minusMinutes(20));
        assertTrue(mensaje(() -> respuestaService.modificar(id, fausto(), "Tarde"))
                .contains("pasó el plazo de edición de 15 minutos"));
        assertFalse(respuesta.puedeModificar(fausto(), 15));
    }

    @Test
    @DisplayName("CU-42: el administrador da de baja una respuesta y la consulta vuelve a estar sin responder")
    void daDeBajaRespuesta() {
        ConsultaForo consulta = primera().getConsultasForoOrdenadas().get(0);
        assertFalse(consulta.estaSinResponder());
        int id = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        respuestaService.darDeBaja(id);

        assertTrue(consulta.estaSinResponder());
        assertTrue(mensaje(() -> respuestaService.darDeBaja(id)).contains("no se encuentra activa"));
    }
}
