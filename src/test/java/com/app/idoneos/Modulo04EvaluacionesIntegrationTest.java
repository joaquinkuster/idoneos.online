package com.app.idoneos;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class Modulo04EvaluacionesIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRespuestaRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired private IntentoAutoevaluacionRepository intentoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-53: Buscar pools de preguntas con filtros")
    void testCU53_BuscarPool() throws Exception {
        mockMvc.perform(get("/evaluaciones/pools"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/evaluaciones/cu-53-buscar-pool"))
                .andExpect(model().attributeExists("pools"))
                .andExpect(model().attributeExists("unidades"));
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-54, CU-55, CU-56: Ciclo de vida completo del Pool de preguntas (Crear, Modificar, Dar de baja)")
    void testCU54_CU55_CU56_CicloDeVidaPool() throws Exception {
        List<Unidad> unidades = unidadRepository.findAll().stream().filter(u -> !u.isBaja()).toList();
        assertFalse(unidades.isEmpty(), "Debe existir al menos una unidad activa");
        Unidad unidad = unidades.get(0);

        // CU-54: Crear Pool
        mockMvc.perform(post("/evaluaciones/pools/guardar")
                        .param("unidadId", String.valueOf(unidad.getId()))
                        .param("nombre", "Pool de Prueba QA Automatizado")
                        .param("pregunta1_texto", "¿Cuál es la función de la CNV?")
                        .param("pregunta1_tipo", "multiple")
                        .param("p1_opt1", "Regular y supervisar los mercados")
                        .param("p1_opt2", "Fijar tipo de cambio")
                        .param("p1_correcta", "1"))
                .andExpect(status().is3xxRedirection());

        Pool poolCreado = poolRepository.findAll().stream()
                .filter(p -> "Pool de Prueba QA Automatizado".equals(p.getNombre()) && !p.isBaja())
                .findFirst()
                .orElse(null);
        assertNotNull(poolCreado, "El pool debe haberse guardado en la base de datos");
        assertEquals(unidad.getId(), poolCreado.getUnidad().getId());

        // CU-55: Modificar Pool
        mockMvc.perform(post("/evaluaciones/pools/" + poolCreado.getId() + "/editar")
                        .param("nombre", "Pool Modificado QA"))
                .andExpect(status().is3xxRedirection());

        Pool poolModificado = poolRepository.findById(poolCreado.getId()).orElseThrow();
        assertEquals("Pool Modificado QA", poolModificado.getNombre());

        // CU-56: Dar de baja Pool
        mockMvc.perform(post("/evaluaciones/pools/" + poolModificado.getId() + "/baja"))
                .andExpect(status().is3xxRedirection());

        Pool poolBaja = poolRepository.findById(poolModificado.getId()).orElseThrow();
        assertTrue(poolBaja.isBaja(), "El pool debe tener la marca de baja lógica en true");
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-57: Buscar autoevaluaciones")
    void testCU57_BuscarAutoevaluacion() throws Exception {
        mockMvc.perform(get("/evaluaciones/autoevaluaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/evaluaciones/cu-57-buscar-autoevaluacion"))
                .andExpect(model().attributeExists("autoevaluaciones"))
                .andExpect(model().attributeExists("unidades"));
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-58, CU-59, CU-60: Ciclo de vida de Autoevaluación (Crear, Modificar, Dar de baja)")
    void testCU58_CU59_CU60_CicloDeVidaAutoevaluacion() throws Exception {
        List<Unidad> unidades = unidadRepository.findAll().stream().filter(u -> !u.isBaja()).toList();
        assertFalse(unidades.isEmpty(), "Debe existir al menos una unidad activa");
        Unidad unidad = unidades.get(0);

        // CU-58: Crear Autoevaluación
        mockMvc.perform(post("/evaluaciones/autoevaluaciones/guardar")
                        .param("unidadId", String.valueOf(unidad.getId()))
                        .param("nombre", "Autoevaluación Test QA")
                        .param("tiempoLimite", "45")
                        .param("cantidadPreguntas", "15")
                        .param("intentosPermitidos", "3"))
                .andExpect(status().is3xxRedirection());

        Autoevaluacion autoCreada = autoevaluacionRepository.findAll().stream()
                .filter(a -> "Autoevaluación Test QA".equals(a.getNombre()) && !a.isBaja())
                .findFirst()
                .orElse(null);
        assertNotNull(autoCreada, "La autoevaluación debe haberse guardado");
        assertEquals(45, autoCreada.getTiempoLimite());
        assertEquals(15, autoCreada.getCantidadPreguntas());

        // CU-59: Modificar Autoevaluación
        mockMvc.perform(post("/evaluaciones/autoevaluaciones/" + autoCreada.getIdAutoevaluacion() + "/editar")
                        .param("nombre", "Autoevaluación Test Modificada QA")
                        .param("tiempoLimite", "60")
                        .param("cantidadPreguntas", "20")
                        .param("intentosPermitidos", "5"))
                .andExpect(status().is3xxRedirection());

        Autoevaluacion autoModificada = autoevaluacionRepository.findById(autoCreada.getIdAutoevaluacion()).orElseThrow();
        assertEquals("Autoevaluación Test Modificada QA", autoModificada.getNombre());
        assertEquals(60, autoModificada.getTiempoLimite());

        // CU-60: Dar de baja Autoevaluación
        mockMvc.perform(post("/evaluaciones/autoevaluaciones/" + autoModificada.getIdAutoevaluacion() + "/baja"))
                .andExpect(status().is3xxRedirection());

        Autoevaluacion autoBaja = autoevaluacionRepository.findById(autoModificada.getIdAutoevaluacion()).orElseThrow();
        assertTrue(autoBaja.isBaja(), "La autoevaluación debe quedar marcada con baja = true");
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-61, CU-62: Supervisar Intentos de alumnos y Ver Calificaciones")
    void testCU61_CU62_SupervisarIntentosYCalificaciones() throws Exception {
        mockMvc.perform(get("/evaluaciones/intentos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/evaluaciones/cu-61-buscar-intento-de-autoevaluacion"))
                .andExpect(model().attributeExists("intentos"));

        mockMvc.perform(get("/evaluaciones/calificaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/evaluaciones/cu-62-ver-calificaciones"))
                .andExpect(model().attributeExists("unidades"));
    }

    @Test
    @WithMockUser(username = "alumno@idoneos.online", roles = {"Alumno"})
    @DisplayName("CU-63: Rendir evaluación con sorteo de preguntas y corrección automática")
    void testCU63_RealizarIntentoAutoevaluacion() throws Exception {
        List<Autoevaluacion> autoevaluaciones = autoevaluacionRepository.findAll().stream().filter(a -> !a.isBaja()).toList();
        if (!autoevaluaciones.isEmpty()) {
            Autoevaluacion ae = autoevaluaciones.get(0);
            Unidad unidad = ae.getUnidad();
            if (unidad != null) {
                Pool pool = poolRepository.findByUnidadAndBajaFalse(unidad).orElseGet(() -> {
                    Pool p = new Pool("Pool Test CU63", unidad);
                    return poolRepository.save(p);
                });
                List<Pregunta> preguntas = preguntaRepository.findByPoolAndBajaFalse(pool);
                if (preguntas.isEmpty()) {
                    Pregunta p = new Pregunta("¿Pregunta Test?", true, pool);
                    p = preguntaRepository.save(p);
                    opcionRespuestaRepository.save(new OpcionRespuesta("Respuesta Correcta", true, p));
                    opcionRespuestaRepository.save(new OpcionRespuesta("Respuesta Incorrecta", false, p));
                }
            }

            mockMvc.perform(get("/evaluaciones/autoevaluaciones/" + ae.getIdAutoevaluacion() + "/rendir"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pages/evaluaciones/cu-63-realizar-intento-de-autoevaluacion"))
                    .andExpect(model().attributeExists("autoevaluacion"))
                    .andExpect(model().attributeExists("preguntas"));
        }
    }

    @Autowired private InscripcionRepository inscripcionRepository;

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-64: Dar de baja / Anular intento de autoevaluación por fraude")
    void testCU64_DarDeBajaIntento() throws Exception {
        List<Autoevaluacion> autoevaluaciones = autoevaluacionRepository.findAll().stream().filter(a -> !a.isBaja()).toList();
        List<Inscripcion> inscripciones = inscripcionRepository.findAll().stream().filter(i -> !i.getBaja()).toList();
        if (!autoevaluaciones.isEmpty() && !inscripciones.isEmpty()) {
            Autoevaluacion ae = autoevaluaciones.get(0);
            Inscripcion inscripcion = inscripciones.get(0);

            IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, ae);
            intento.setFechaEntrega(LocalDateTime.now());
            intento.setNota(10.0f);
            intento.setBaja(false);
            intento = intentoRepository.save(intento);

            mockMvc.perform(post("/evaluaciones/intentos/" + intento.getId() + "/baja"))
                    .andExpect(status().is3xxRedirection());

            IntentoAutoevaluacion intentoBaja = intentoRepository.findById(intento.getId()).orElseThrow();
            assertTrue(intentoBaja.isBaja(), "El intento debe quedar marcado como anulado / dado de baja");
        }
    }
}
