package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.IntentoAutoevaluacion.IntentoAutoevaluacionServicio;

/**
 * Pruebas de CU-70 (realizar un intento) con los datos de la semilla. Valentina Ruiz cursa "Mercado de Capitales Argentino"
 * (cohorte en dictado): tiene completada el tema 1, cuya autoevaluación permite navegar libremente y salir (5 preguntas,
 * nota mínima 6, 3 intentos de los que ya usó 2); el tema 2 está habilitada y su autoevaluación es secuencial, descuenta
 * por error, no permite salir y dura 20 minutos (5 preguntas, nota mínima 7); el tema 3 todavía está bloqueada.
 */
@SpringBootTest
@Transactional
class IntentoAutoevaluacionServicioTest {

    @Autowired
    private IntentoAutoevaluacionServicio intentoServicio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Inscripcion valentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
    }

    private Alumno alumno() {
        return valentina().getAlumno();
    }

    private Autoevaluacion autoevaluacion(String nombre) {
        return autoevaluacionRepositorio.findAll().stream().filter(a -> a.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Autoevaluacion libre() {
        return autoevaluacion("Autoevaluación Tema 1: Marco regulatorio");
    }

    private Autoevaluacion secuencial() {
        return autoevaluacion("Autoevaluación Tema 2: Ejercicios de rendimiento");
    }

    private void assertError(String fragmento, Runnable accion) {
        String mensaje = assertThrows(IllegalArgumentException.class, accion::run).getMessage();
        assertTrue(mensaje.contains(fragmento), mensaje);
    }

    private IntentoAutoevaluacion iniciar(Autoevaluacion autoevaluacion) {
        return intentoServicio.iniciarIntento(alumno(), autoevaluacion.getIdAutoevaluacion());
    }

    private List<Integer> correctas(PreguntaIntento pregunta) {
        return pregunta.getPregunta().getOpcionesActivas().stream().filter(OpcionRespuesta::getEsCorrecta)
                .map(OpcionRespuesta::getIdOpcionRespuesta).toList();
    }

    private List<Integer> incorrecta(PreguntaIntento pregunta) {
        return List.of(pregunta.getPregunta().getOpcionesActivas().stream().filter(o -> !o.getEsCorrecta()).findFirst()
                .orElseThrow().getIdOpcionRespuesta());
    }

    @Test
    @DisplayName("Inicia un intento: sortea las preguntas distintas con su orden y, si ya hay uno en curso, lo retoma")
    void iniciarYRetomar() {
        IntentoAutoevaluacion intento = iniciar(libre());

        assertTrue(intento.estaEnCurso());
        assertEquals(5, intento.getPreguntasIntento().size());
        assertEquals(List.of(1, 2, 3, 4, 5), intento.getPreguntasOrdenadas().stream().map(PreguntaIntento::getOrden).toList());
        assertEquals(5, new HashSet<>(intento.getPreguntasOrdenadas().stream().map(p -> p.getPregunta().getIdPregunta()).toList()).size());
        assertTrue(intento.getSegundosRestantes(LocalDateTime.now()) > 29 * 60); // 30 minutos desde el inicio

        // Un segundo inicio no sortea de nuevo ni consume otro intento
        assertEquals(intento.getIdIntentoAutoevaluacion(), iniciar(libre()).getIdIntentoAutoevaluacion());
        assertEquals(3, libre().intentosDe(valentina()).size()); // los 2 de la semilla y este
    }

    @Test
    @DisplayName("No deja iniciar con el tema bloqueado, fuera del período, sin intentos o sin preguntas suficientes")
    void precondiciones() {
        Autoevaluacion tercera = autoevaluacion("Autoevaluación del tema 3");
        assertError("Todavía no podés acceder", () -> iniciar(tercera)); // el tema 3 está bloqueada
        assertTrue(intentoServicio.motivoNoDisponible(alumno(), tercera).isPresent());

        Autoevaluacion libre = libre();
        libre.setFechaApertura(LocalDateTime.now().plusDays(2));
        assertError("se abre el", () -> iniciar(libre));
        libre.setFechaApertura(LocalDateTime.now().minusDays(3));
        libre.setFechaCierre(LocalDateTime.now().minusDays(1));
        assertError("cerró el", () -> iniciar(libre));
        libre.setFechaCierre(null);

        libre.setIntentosPermitidos(2); // ya usó 2
        assertError("Ya usaste 2 intentos permitidos", () -> iniciar(libre));
        assertTrue(intentoServicio.motivoNoDisponible(alumno(), libre).isPresent());
        libre.setIntentosPermitidos(3);
        assertTrue(intentoServicio.motivoNoDisponible(alumno(), libre).isEmpty());

        libre.setCantidadPreguntas(50); // los pools no reúnen tantas
        assertError("no tiene preguntas suficientes", () -> iniciar(libre));
        libre.setCantidadPreguntas(5);

        libre.setOculto(true);
        assertError("no se encuentra disponible", () -> iniciar(libre));
    }

    @Test
    @DisplayName("Guarda y reemplaza las respuestas; una opción única no admite varias ni opciones de otra pregunta")
    void responderPreguntas() {
        IntentoAutoevaluacion intento = iniciar(libre());
        int id = intento.getIdIntentoAutoevaluacion();
        PreguntaIntento unica = intento.getPreguntasOrdenadas().stream().filter(p -> !p.getPregunta().getEsOpcionMultiple()).findFirst().orElseThrow();
        PreguntaIntento otra = intento.getPreguntasOrdenadas().stream().filter(p -> p != unica).findFirst().orElseThrow();

        intentoServicio.responderPregunta(id, alumno(), unica.getIdPreguntaIntento(), correctas(unica));
        assertTrue(unica.estaRespondida());
        assertEquals(1, intento.getCantidadRespondidas());

        intentoServicio.responderPregunta(id, alumno(), unica.getIdPreguntaIntento(), incorrecta(unica)); // reemplaza
        assertEquals(1, unica.getRespuestasIntento().size());
        assertFalse(unica.getOpcionesElegidas().get(0).getEsCorrecta());

        List<Integer> dos = unica.getPregunta().getOpcionesActivas().stream().limit(2).map(OpcionRespuesta::getIdOpcionRespuesta).toList();
        assertError("una sola opción", () -> intentoServicio.responderPregunta(id, alumno(), unica.getIdPreguntaIntento(), dos));
        assertError("no pertenece a la pregunta", () -> intentoServicio.responderPregunta(id, alumno(), unica.getIdPreguntaIntento(),
                List.of(otra.getPregunta().getOpcionesActivas().get(0).getIdOpcionRespuesta())));
        assertError("no pertenece al intento", () -> intentoServicio.responderPregunta(id, alumno(), 999999, correctas(unica)));

        intentoServicio.responderPregunta(id, alumno(), unica.getIdPreguntaIntento(), List.of()); // limpiar
        assertFalse(unica.estaRespondida());

        // Con navegación libre se puede marcar para revisar
        intentoServicio.marcarPregunta(id, alumno(), otra.getIdPreguntaIntento(), true);
        assertTrue(otra.getMarcada());
        assertEquals(1, intento.getCantidadMarcadas());
    }

    @Test
    @DisplayName("En un cuestionario secuencial solo se responde la pregunta actual y no se puede marcar ni volver")
    void cuestionarioSecuencial() {
        IntentoAutoevaluacion intento = iniciar(secuencial());
        int id = intento.getIdIntentoAutoevaluacion();
        List<PreguntaIntento> preguntas = intento.getPreguntasOrdenadas();
        assertEquals(preguntas.get(0), intento.getPreguntaActualSecuencial());

        assertError("solo podés responder la pregunta actual", () -> intentoServicio.responderPregunta(id, alumno(),
                preguntas.get(1).getIdPreguntaIntento(), correctas(preguntas.get(1))));
        assertError("no permite marcar", () -> intentoServicio.marcarPregunta(id, alumno(), preguntas.get(0).getIdPreguntaIntento(), true));

        intentoServicio.responderPregunta(id, alumno(), preguntas.get(0).getIdPreguntaIntento(), correctas(preguntas.get(0)));
        intentoServicio.avanzarPregunta(id, alumno()); // pasa a la 2
        assertEquals(preguntas.get(1), intento.getPreguntaActualSecuencial());
        assertTrue(preguntas.get(0).getPasada());
        assertError("solo podés responder la pregunta actual", () -> intentoServicio.responderPregunta(id, alumno(),
                preguntas.get(0).getIdPreguntaIntento(), incorrecta(preguntas.get(0)))); // no se vuelve atrás

        intentoServicio.avanzarPregunta(id, alumno()); // la 2 pasa sin responder
        assertTrue(preguntas.get(1).getPasada());
        assertFalse(preguntas.get(1).estaRespondida());
        for (int i = 2; i < preguntas.size(); i++) {
            intentoServicio.avanzarPregunta(id, alumno());
        }
        assertEquals(null, intento.getPreguntaActualSecuencial()); // ya pasó por todas: solo falta entregar
    }

    @Test
    @DisplayName("Entregar corrige el intento; si aprobó la única autoevaluación del tema, la completa y habilita la siguiente")
    void entregarAprobado() {
        Inscripcion inscripcion = valentina();
        Autoevaluacion secuencial = secuencial();
        IntentoAutoevaluacion intento = iniciar(secuencial);
        int id = intento.getIdIntentoAutoevaluacion();
        assertFalse(inscripcion.estaTemaCompletada(secuencial.getTema()));

        for (PreguntaIntento pregunta : intento.getPreguntasOrdenadas()) {
            intentoServicio.responderPregunta(id, alumno(), pregunta.getIdPreguntaIntento(), correctas(pregunta));
            intentoServicio.avanzarPregunta(id, alumno());
        }
        IntentoAutoevaluacion entregado = intentoServicio.entregarIntento(id, alumno());

        assertTrue(entregado.estaFinalizado());
        assertEquals(10f, entregado.getNota());
        assertTrue(entregado.getAprobado());
        assertNotNull(entregado.getFechaEntrega());
        assertTrue(inscripcion.estaTemaCompletada(secuencial.getTema()));
        assertTrue(inscripcion.estaTemaHabilitada(inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(2)));
        assertError("ya fue entregado", () -> intentoServicio.responderPregunta(id, alumno(),
                intento.getPreguntasOrdenadas().get(0).getIdPreguntaIntento(), List.of()));
        // Entregar de nuevo no repite la corrección
        assertEquals(10f, intentoServicio.entregarIntento(id, alumno()).getNota());
    }

    @Test
    @DisplayName("Entregar sin responder cuenta las preguntas como vacías: nota 0 y el tema no se completa")
    void entregarSinResponder() {
        Inscripcion inscripcion = valentina();
        IntentoAutoevaluacion intento = iniciar(secuencial());

        IntentoAutoevaluacion entregado = intentoServicio.entregarIntento(intento.getIdIntentoAutoevaluacion(), alumno());

        assertEquals(0f, entregado.getNota());
        assertFalse(entregado.getAprobado());
        assertFalse(inscripcion.estaTemaCompletada(secuencial().getTema()));
        assertEquals(1, secuencial().intentosDe(inscripcion).size());
    }

    @Test
    @DisplayName("Con el tiempo agotado el intento se cierra solo y se corrige; hay una tolerancia para las entregas con demora")
    void tiempoAgotado() {
        IntentoAutoevaluacion intento = iniciar(secuencial()); // 20 minutos
        int id = intento.getIdIntentoAutoevaluacion();
        PreguntaIntento primera = intento.getPreguntasOrdenadas().get(0);
        assertEquals(15, intentoServicio.obtenerGraciaEnSegundos());

        // Pasado el tiempo pero dentro de la tolerancia todavía se acepta la respuesta
        intento.setFechaCreacion(LocalDateTime.now().minusMinutes(20).minusSeconds(5));
        intentoServicio.responderPregunta(id, alumno(), primera.getIdPreguntaIntento(), correctas(primera));
        assertTrue(intento.estaEnCurso());

        // Pasada la tolerancia se cierra y se corrige con lo guardado
        intento.setFechaCreacion(LocalDateTime.now().minusMinutes(21));
        IntentoAutoevaluacion cerrado = intentoServicio.buscarPropio(id, alumno());
        assertTrue(cerrado.estaFinalizado());
        assertEquals(1, cerrado.getCantidadRespondidas());
        assertNotNull(cerrado.getNota());
        assertError("Se agotó el tiempo", () -> intentoServicio.responderPregunta(id, alumno(),
                primera.getIdPreguntaIntento(), incorrecta(primera)));
    }

    @Test
    @DisplayName("El intento que no permite salir bloquea al alumno hasta que termina; el que permite salir, no")
    void intentoQueNoPermiteSalir() {
        assertTrue(intentoServicio.buscarEnCursoSinSalida(alumno()).isEmpty());

        iniciar(libre()); // permite salir
        assertTrue(intentoServicio.buscarEnCursoSinSalida(alumno()).isEmpty());

        IntentoAutoevaluacion sinSalida = iniciar(secuencial());
        Optional<IntentoAutoevaluacion> bloqueante = intentoServicio.buscarEnCursoSinSalida(alumno());
        assertEquals(sinSalida.getIdIntentoAutoevaluacion(), bloqueante.orElseThrow().getIdIntentoAutoevaluacion());

        intentoServicio.entregarIntento(sinSalida.getIdIntentoAutoevaluacion(), alumno());
        assertTrue(intentoServicio.buscarEnCursoSinSalida(alumno()).isEmpty());

        // Cuando se agota el tiempo el bloqueo también se levanta
        IntentoAutoevaluacion otro = iniciar(secuencial());
        otro.setFechaCreacion(LocalDateTime.now().minusHours(1));
        assertTrue(intentoServicio.buscarEnCursoSinSalida(alumno()).isEmpty());
        assertTrue(otro.estaFinalizado());
    }

    @Test
    @DisplayName("La tarea periódica cierra solo los intentos vencidos")
    void cerrarIntentosVencidos() {
        IntentoAutoevaluacion vencido = iniciar(secuencial());
        IntentoAutoevaluacion vigente = iniciar(libre());
        vencido.setFechaCreacion(LocalDateTime.now().minusHours(2));

        assertEquals(1, intentoServicio.cerrarIntentosVencidos());

        assertTrue(vencido.estaFinalizado());
        assertTrue(vigente.estaEnCurso());
        assertNotEquals(vencido.getIdIntentoAutoevaluacion(), vigente.getIdIntentoAutoevaluacion());
    }

    @Test
    @DisplayName("Un intento solo lo puede ver y modificar su dueño")
    void soloElDueno() {
        IntentoAutoevaluacion intento = iniciar(libre());
        Alumno otro = inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("martin.gomez@correo.com")).findFirst().orElseThrow().getAlumno();

        assertError("no existe", () -> intentoServicio.buscarPropio(intento.getIdIntentoAutoevaluacion(), otro));
        assertError("no existe", () -> intentoServicio.entregarIntento(intento.getIdIntentoAutoevaluacion(), otro));
        assertError("no existe", () -> intentoServicio.buscarPropio(999999, alumno()));
    }
}
