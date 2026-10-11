package com.app.idoneos.servicio;

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

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.RespuestaForo;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.AlumnoRepositorio;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.RespuestaForoRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.ConsultaForo.ConsultaForoServicio;
import com.app.idoneos.servicio.RespuestaForo.RespuestaForoServicio;

/**
 * Pruebas del foro (CU-35 a CU-42) con los datos de la semilla: la unidad 1 de "Mercado de Capitales Argentino" tiene una
 * consulta de Valentina Ruiz con una respuesta de Fausto Spotorno; la unidad 2, una consulta sin responder; la 3 no tiene
 * consultas. Valentina tiene habilitadas las unidades 1 y 2; el plazo de edición es de 15 minutos.
 */
@SpringBootTest
@Transactional
class ForoServicioTest {

    @Autowired
    private ConsultaForoServicio consultaServicio;

    @Autowired
    private RespuestaForoServicio respuestaServicio;

    @Autowired
    private ConsultaForoRepositorio consultaRepositorio;

    @Autowired
    private RespuestaForoRepositorio respuestaRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private AlumnoRepositorio alumnoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Alumno alumno(String correo) {
        return alumnoRepositorio.findAll().stream().filter(a -> a.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Unidad unidad(String curso, String titulo) {
        return unidadRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private Unidad primera() {
        return unidad("Mercado de Capitales Argentino", "Introducción al mercado de capitales");
    }

    private Unidad segunda() {
        return unidad("Mercado de Capitales Argentino", "Renta fija y bonos");
    }

    private Unidad tercera() {
        return unidad("Mercado de Capitales Argentino", "Renta variable y opciones");
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

    // ------------------------------------------------------------------ CU-35 Buscar consulta

    @Test
    @DisplayName("CU-35: se buscan las consultas por texto y por rango de fechas, sin distinguir mayúsculas ni tildes")
    void busca() {
        assertEquals(1, consultaServicio.buscarConFiltros(primera(), null, null, null).size());
        assertEquals(1, consultaServicio.buscarConFiltros(primera(), "MATRICULA", null, null).size());
        assertTrue(consultaServicio.buscarConFiltros(primera(), "duration", null, null).isEmpty());

        LocalDate hoy = LocalDate.now();
        // la consulta de la unidad 1 es de hace 3 días
        assertEquals(1, consultaServicio.buscarConFiltros(primera(), null, hoy.minusDays(4), hoy.minusDays(2)).size());
        assertTrue(consultaServicio.buscarConFiltros(primera(), null, hoy.minusDays(2), null).isEmpty());
        assertTrue(consultaServicio.buscarConFiltros(primera(), null, null, hoy.minusDays(4)).isEmpty());
    }

    @Test
    @DisplayName("CU-35: las consultas se listan con el último mensaje más reciente primero")
    void ordenaPorUltimoMensaje() {
        ConsultaForo nueva = consultaServicio.registrarConsulta(valentina(), primera().getIdUnidad(), "Otra consulta");

        List<ConsultaForo> consultas = consultaServicio.buscarConFiltros(primera(), null, null, null);

        assertEquals(nueva.getIdConsulta(), consultas.get(0).getIdConsulta());
        assertEquals(2, consultas.size());
    }

    // ------------------------------------------------------------------ CU-36 Registrar consulta

    @Test
    @DisplayName("CU-36: el alumno con la unidad habilitada registra una consulta")
    void registraConsulta() {
        ConsultaForo consulta = consultaServicio.registrarConsulta(valentina(), segunda().getIdUnidad(),
                "  ¿Qué es el cupón corrido?  ");

        assertTrue(consulta.getIdConsulta() > 0);
        assertEquals("¿Qué es el cupón corrido?", consulta.getTexto());
        assertEquals("Valentina Ruiz", consulta.getAutor());
        assertTrue(consulta.estaSinResponder());
        assertEquals(2, segunda().getCantidadConsultasForo());
    }

    @Test
    @DisplayName("CU-36: falta el texto, supera el máximo o el alumno no tiene la unidad habilitada")
    void validaAlRegistrar() {
        int unidad = segunda().getIdUnidad();
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(valentina(), unidad, " "))
                .contains("Debe completar el texto de la consulta."));
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(valentina(), unidad, "x".repeat(501)))
                .contains("no puede superar los 500 caracteres"));
        assertNotNull(consultaServicio.registrarConsulta(valentina(), unidad, "x".repeat(500)));

        // unidad bloqueada, dictado sin comenzar, sin inscripción, sin unidad y sin ser alumno
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(valentina(), tercera().getIdUnidad(), "Hola"))
                .contains("No tenés una inscripción vigente"));
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(alumno("martin.gomez@correo.com"), unidad, "Hola"))
                .contains("No tenés una inscripción vigente"));
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(alumno("tomas.herrera@correo.com"), unidad, "Hola"))
                .contains("No tenés una inscripción vigente"));
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(valentina(), null, "Hola"))
                .contains("La unidad no se encuentra activa"));
        assertTrue(mensaje(() -> consultaServicio.registrarConsulta(null, unidad, "Hola")).contains("rol de alumno"));
    }

    // ------------------------------------------------------------------ CU-37 Modificar consulta

    @Test
    @DisplayName("CU-37: el alumno modifica su consulta dentro del plazo")
    void modificaConsulta() {
        ConsultaForo consulta = consultaServicio.registrarConsulta(valentina(), segunda().getIdUnidad(), "Texto");

        ConsultaForo modificada = consultaServicio.modificarConsulta(consulta.getIdConsulta(), valentina(), "Texto nuevo");

        assertEquals("Texto nuevo", modificada.getTexto());
        assertNotNull(modificada.getUltimaModificacion());
        assertTrue(modificada.puedeModificar(valentina(), 15));
    }

    @Test
    @DisplayName("CU-37: no se modifica una consulta ajena, vencida, vacía o dada de baja")
    void validaAlModificarConsulta() {
        ConsultaForo consulta = consultaServicio.registrarConsulta(valentina(), segunda().getIdUnidad(), "Texto");
        int id = consulta.getIdConsulta();

        assertTrue(mensaje(() -> consultaServicio.modificarConsulta(id, alumno("tomas.herrera@correo.com"), "Otro"))
                .contains("tus propias consultas"));
        assertTrue(mensaje(() -> consultaServicio.modificarConsulta(id, valentina(), " "))
                .contains("Debe completar el texto de la consulta."));

        consulta.setFechaCreacion(LocalDateTime.now().minusMinutes(16));
        assertTrue(mensaje(() -> consultaServicio.modificarConsulta(id, valentina(), "Tarde"))
                .contains("pasó el plazo de edición de 15 minutos"));
        assertFalse(consulta.puedeModificar(valentina(), 15));

        consultaServicio.darDeBajaConsulta(id);
        assertTrue(mensaje(() -> consultaServicio.modificarConsulta(id, valentina(), "Baja"))
                .contains("no se encuentra activa"));
    }

    // ------------------------------------------------------------------ CU-38 Dar de baja consulta

    @Test
    @DisplayName("CU-38: dar de baja una consulta también da de baja sus respuestas")
    void daDeBajaConsultaConRespuestas() {
        ConsultaForo consulta = primera().getConsultasForoOrdenadas().get(0);
        int idRespuesta = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        consultaServicio.darDeBajaConsulta(consulta.getIdConsulta());

        assertTrue(consultaRepositorio.findById(consulta.getIdConsulta()).orElseThrow().getBaja());
        assertTrue(respuestaRepositorio.findById(idRespuesta).orElseThrow().getBaja());
        assertEquals(0, primera().getCantidadConsultasForo());
        assertTrue(mensaje(() -> consultaServicio.darDeBajaConsulta(consulta.getIdConsulta()))
                .contains("no se encuentra activa"));
    }

    // ------------------------------------------------------------------ CU-40 a CU-42 Respuestas

    @Test
    @DisplayName("CU-40: el docente del curso responde una consulta y esta deja de estar sin responder")
    void respondeConsulta() {
        ConsultaForo consulta = segunda().getConsultasForoOrdenadas().get(0);
        assertTrue(consulta.estaSinResponder());

        RespuestaForo respuesta = respuestaServicio.registrarRespuesta(fausto(), consulta.getIdConsulta(),
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

        assertTrue(mensaje(() -> respuestaServicio.registrarRespuesta(fausto(), id, ""))
                .contains("Debe completar el texto de la respuesta."));
        assertTrue(mensaje(() -> respuestaServicio.registrarRespuesta(fausto(), id, "x".repeat(501)))
                .contains("no puede superar los 500 caracteres"));
        assertTrue(mensaje(() -> respuestaServicio.registrarRespuesta(null, id, "Hola")).contains("rol de docente"));
        assertTrue(mensaje(() -> respuestaServicio.registrarRespuesta(fausto(), 999999, "Hola"))
                .contains("La consulta no se encuentra activa"));

        // una consulta del curso "Macroeconomía de Coyuntura", donde Sebastián Bordato no participa
        ConsultaForo ajena = consultaRepositorio.save(new ConsultaForo(
                unidad("Macroeconomía de Coyuntura", "Inflación y precios"), consulta.getInscripcion(), "Consulta"));
        assertTrue(mensaje(() -> respuestaServicio.registrarRespuesta(docente("sebastian.bordato@idoneos.online"),
                ajena.getIdConsulta(), "Hola")).contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-41: el docente modifica su respuesta dentro del plazo, pero no la ajena ni la vencida")
    void modificaRespuesta() {
        ConsultaForo consulta = segunda().getConsultasForoOrdenadas().get(0);
        RespuestaForo respuesta = respuestaServicio.registrarRespuesta(fausto(), consulta.getIdConsulta(), "Texto");
        int id = respuesta.getIdRespuesta();

        assertEquals("Texto nuevo", respuestaServicio.modificarRespuesta(id, fausto(), "Texto nuevo").getTexto());
        // Sebastián participa en el curso (ayudante), pero la respuesta no es suya
        assertTrue(mensaje(() -> respuestaServicio.modificarRespuesta(id, docente("sebastian.bordato@idoneos.online"),
                "Otro")).contains("tus propias respuestas"));
        assertTrue(mensaje(() -> respuestaServicio.modificarRespuesta(id, fausto(), " "))
                .contains("Debe completar el texto de la respuesta."));

        respuesta.setFechaCreacion(LocalDateTime.now().minusMinutes(20));
        assertTrue(mensaje(() -> respuestaServicio.modificarRespuesta(id, fausto(), "Tarde"))
                .contains("pasó el plazo de edición de 15 minutos"));
        assertFalse(respuesta.puedeModificar(fausto(), 15));
    }

    @Test
    @DisplayName("CU-42: el administrador da de baja una respuesta y la consulta vuelve a estar sin responder")
    void daDeBajaRespuesta() {
        ConsultaForo consulta = primera().getConsultasForoOrdenadas().get(0);
        assertFalse(consulta.estaSinResponder());
        int id = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        respuestaServicio.darDeBajaRespuesta(id);

        assertTrue(consulta.estaSinResponder());
        assertTrue(mensaje(() -> respuestaServicio.darDeBajaRespuesta(id)).contains("no se encuentra activa"));
    }
}
