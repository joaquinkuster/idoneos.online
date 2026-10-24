package com.app.idoneos.controlador;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Comparator;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;

/**
 * Pruebas de las pantallas del alumno para CU-70: la ficha de la autoevaluación, el cuestionario, el resumen, la entrega y el
 * bloqueo del resto de la plataforma mientras rinde una autoevaluación que no permite salir. Valentina Ruiz tiene la unidad
 * 2 habilitada (autoevaluación secuencial que no permite salir) y la 1 completada (navegación libre, permite salir).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntentoAutoevaluacionControladorTest {

    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.app.idoneos.repositorio.InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private com.app.idoneos.repositorio.ProgresoRepositorio progresoRepositorio;

    @Autowired
    private com.app.idoneos.servicio.IntentoAutoevaluacion.IntentoAutoevaluacionServicio intentoServicio;

    @Autowired
    private com.app.idoneos.servicio.Certificado.CertificadoServicio certificadoServicio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoRepositorio;

    private int autoevaluacion(String nombre) {
        return autoevaluacionRepositorio.findAll().stream().filter(a -> a.getNombre().equals(nombre))
                .mapToInt(Autoevaluacion::getIdAutoevaluacion).findFirst().orElseThrow();
    }

    private int libre() {
        return autoevaluacion("Autoevaluación Unidad 1: Marco regulatorio");
    }

    private int secuencial() {
        return autoevaluacion("Autoevaluación Unidad 2: Ejercicios de rendimiento");
    }

    private IntentoAutoevaluacion ultimoIntento() {
        return intentoRepositorio.findAll().stream().max(Comparator.comparingInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion))
                .orElseThrow();
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La ficha muestra las reglas, el historial de intentos con su mejor nota y el botón para rendir")
    void ficha() throws Exception {
        mockMvc.perform(get("/autoevaluacion/ver/" + libre()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Antes de empezar")))
                .andExpect(content().string(Matchers.containsString("hasta")))
                .andExpect(content().string(Matchers.containsString("Resumen de sus intentos anteriores")))
                .andExpect(content().string(Matchers.containsString("4,00")))
                .andExpect(content().string(Matchers.containsString("8,00 / 10,00")))
                .andExpect(content().string(Matchers.containsString("Realizar intento")))
                .andExpect(content().string(Matchers.containsString("Podés salir y volver al intento")));

        mockMvc.perform(get("/autoevaluacion/ver/" + secuencial()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("No permite salir")))
                .andExpect(content().string(Matchers.containsString("data-sin-salida=\"true\"")))
                .andExpect(content().string(Matchers.containsString("Todavía no realizaste ningún intento")));

        // La unidad 3 todavía está bloqueada para Valentina
        mockMvc.perform(get("/autoevaluacion/ver/" + autoevaluacion("Autoevaluación de la unidad 3")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Con navegación libre el cuestionario muestra la grilla y se puede marcar; salir de él no está bloqueado")
    void cuestionarioConNavegacionLibre() throws Exception {
        mockMvc.perform(post("/intento/iniciar/" + libre())).andExpect(status().isFound());
        IntentoAutoevaluacion intento = ultimoIntento();
        int id = intento.getIdIntentoAutoevaluacion();
        PreguntaIntento tercera = intento.getPreguntasOrdenadas().get(2);

        mockMvc.perform(get("/intento/realizar/" + id).param("pregunta", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Pregunta 3")))
                .andExpect(content().string(Matchers.containsString("in-nav-grilla")))
                .andExpect(content().string(Matchers.containsString("Marcar para revisar")))
                .andExpect(content().string(Matchers.containsString("data-reloj")))
                .andExpect(content().string(Matchers.containsString("Vale <strong>2,00</strong>")));

        mockMvc.perform(post("/intento/marcar/" + id).param("preguntaIntentoId", String.valueOf(tercera.getIdPreguntaIntento()))
                .param("marcada", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marcadas").value(1));

        mockMvc.perform(get("/intento/resumen/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Resumen del intento")))
                .andExpect(content().string(Matchers.containsString("Para revisar")))
                .andExpect(content().string(Matchers.containsString("Entregar todo y terminar")));

        // Esta autoevaluación permite salir: el resto de la plataforma sigue accesible
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un cuestionario que no permite salir bloquea cualquier otra vista hasta entregar; cerrar sesión sigue permitido")
    void bloqueoDelExamen() throws Exception {
        mockMvc.perform(post("/intento/iniciar/" + secuencial())).andExpect(status().isFound());
        int id = ultimoIntento().getIdIntentoAutoevaluacion();

        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isFound())
                .andExpect(redirectedUrl("/intento/realizar/" + id));
        mockMvc.perform(get("/inicio")).andExpect(status().isFound()).andExpect(redirectedUrl("/intento/realizar/" + id));
        mockMvc.perform(get("/autoevaluacion/ver/" + secuencial())).andExpect(status().isFound())
                .andExpect(redirectedUrl("/intento/realizar/" + id));

        // Mientras rinde, el cuestionario sí se muestra, en orden y sin "Anterior" ni marcas
        mockMvc.perform(get("/intento/realizar/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Pregunta 1")))
                .andExpect(content().string(Matchers.containsString("data-avanzar")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Marcar para revisar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("in-nav-grilla"))))
                .andExpect(content().string(Matchers.containsString("Una opción incorrecta resta")));
        mockMvc.perform(get("/intento/realizar/" + id).param("pregunta", "4")).andExpect(status().isFound())
                .andExpect(redirectedUrl("/intento/realizar/" + id));

        // Al entregar se levanta el bloqueo
        mockMvc.perform(post("/intento/entregar/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprobado").value(false))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Tu nota fue 0,00 / 10")))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Debés reintentar el cuestionario completo")))
                .andExpect(jsonPath("$.destino").value("/autoevaluacion/ver/" + secuencial()));
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk());
        mockMvc.perform(get("/intento/realizar/" + id)).andExpect(status().isFound())
                .andExpect(redirectedUrl("/autoevaluacion/ver/" + secuencial()));
        mockMvc.perform(post("/logout")).andExpect(status().isFound()); // cerrar sesión no está bloqueado
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Responder devuelve el tiempo que queda según el servidor; avanzar lleva a la siguiente pregunta y al final al resumen")
    void responderYAvanzar() throws Exception {
        mockMvc.perform(post("/intento/iniciar/" + secuencial())).andExpect(status().isFound());
        IntentoAutoevaluacion intento = ultimoIntento();
        int id = intento.getIdIntentoAutoevaluacion();
        PreguntaIntento primera = intento.getPreguntasOrdenadas().get(0);
        PreguntaIntento segunda = intento.getPreguntasOrdenadas().get(1);
        String opcion = String.valueOf(primera.getPregunta().getOpcionesActivas().get(0).getIdOpcionRespuesta());

        mockMvc.perform(post("/intento/responder/" + id).param("preguntaIntentoId", String.valueOf(primera.getIdPreguntaIntento()))
                .param("opcionIds", opcion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respondidas").value(1))
                .andExpect(jsonPath("$.enCurso").value(true))
                .andExpect(jsonPath("$.segundosRestantes").value(Matchers.greaterThan(1100)));

        // Otra pregunta que no es la actual se rechaza
        mockMvc.perform(post("/intento/responder/" + id).param("preguntaIntentoId", String.valueOf(segunda.getIdPreguntaIntento()))
                .param("opcionIds", opcion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("pregunta actual")));

        mockMvc.perform(get("/intento/tiempo/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.segundosRestantes").value(Matchers.lessThanOrEqualTo(1200)));

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/intento/avanzar/" + id)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.destino").value("/intento/realizar/" + id));
        }
        mockMvc.perform(post("/intento/avanzar/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.destino").value("/intento/resumen/" + id));
        mockMvc.perform(get("/intento/realizar/" + id)).andExpect(status().isFound())
                .andExpect(redirectedUrl("/intento/resumen/" + id));
        assertTrue(intentoRepositorio.findById(id).orElseThrow().getPreguntasOrdenadas().stream().allMatch(PreguntaIntento::getPasada));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Desde el curso, el alumno llega a la ficha de cada autoevaluación de las unidades que tiene habilitadas")
    void enlaceDesdeElCurso() throws Exception {
        int inscripcion = inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow().getIdInscripcion();
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcion))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("/autoevaluacion/ver/" + libre())))
                .andExpect(content().string(Matchers.containsString("/autoevaluacion/ver/" + secuencial())));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al entregar el intento que completa el último contenido del curso, se emite el certificado y la respuesta lo informa")
    void entregarElIntentoQueFinalizaElCurso() throws Exception {
        com.app.idoneos.modelo.Inscripcion inscripcion = inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
        // Le quedan sin completar la unidad 2 (la que va a rendir) y la 3: se da la 3 por completada
        com.app.idoneos.modelo.Unidad unidad3 = autoevaluacionRepositorio.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación de la unidad 3")).findFirst().orElseThrow().getUnidad();
        com.app.idoneos.modelo.Progreso progreso = new com.app.idoneos.modelo.Progreso(unidad3, inscripcion);
        progreso.setCompletada(true);
        progreso.setFechaCompletada(java.time.LocalDateTime.now());
        inscripcion.getProgresos().add(progresoRepositorio.save(progreso));
        org.junit.jupiter.api.Assertions.assertNull(inscripcion.getCertificadoVigente());

        // Rinde la unidad 2 con todo correcto
        mockMvc.perform(post("/intento/iniciar/" + secuencial())).andExpect(status().isFound());
        IntentoAutoevaluacion intento = ultimoIntento();
        int id = intento.getIdIntentoAutoevaluacion();
        for (PreguntaIntento pregunta : intento.getPreguntasOrdenadas()) {
            intentoServicio.responderPregunta(id, inscripcion.getAlumno(), pregunta.getIdPreguntaIntento(),
                    pregunta.getPregunta().getOpcionesActivas().stream().filter(o -> o.getEsCorrecta())
                            .map(o -> o.getIdOpcionRespuesta()).toList());
            intentoServicio.avanzarPregunta(id, inscripcion.getAlumno());
        }

        mockMvc.perform(post("/intento/entregar/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprobado").value(true))
                .andExpect(jsonPath("$.cursoFinalizado").value(true))
                .andExpect(jsonPath("$.destino").value("/autoevaluacion/ver/" + secuencial() + "?cursoFinalizado"));
        org.junit.jupiter.api.Assertions.assertNotNull(inscripcion.getCertificadoVigente());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La ficha avisa que se finalizó el curso solo si llega el parámetro y la inscripción tiene su certificado")
    void avisoDeCursoFinalizado() throws Exception {
        com.app.idoneos.modelo.Inscripcion inscripcion = inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
        // Sin certificado, el parámetro no muestra nada
        mockMvc.perform(get("/autoevaluacion/ver/" + libre()).param("cursoFinalizado", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("mostrarCursoFinalizado("))));

        certificadoServicio.emitirCertificado(inscripcion);
        mockMvc.perform(get("/autoevaluacion/ver/" + libre()).param("cursoFinalizado", ""))
                .andExpect(content().string(Matchers.containsString("mostrarCursoFinalizado(")))
                .andExpect(content().string(Matchers.containsString("/certificado/misCertificados")));
        // Sin el parámetro (por ejemplo, al recargar), tampoco
        mockMvc.perform(get("/autoevaluacion/ver/" + libre()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("mostrarCursoFinalizado("))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente no rinde autoevaluaciones")
    void elDocenteNoRinde() throws Exception {
        mockMvc.perform(post("/intento/iniciar/" + libre())).andExpect(status().isForbidden());
        mockMvc.perform(get("/autoevaluacion/ver/" + libre())).andExpect(status().isForbidden());
    }
}
