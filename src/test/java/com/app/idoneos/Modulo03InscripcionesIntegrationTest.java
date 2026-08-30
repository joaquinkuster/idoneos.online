package com.app.idoneos;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.CohorteRepository;
import com.app.idoneos.repository.modulo_cursos.CursoRepository;
import com.app.idoneos.repository.modulo_gestion_academica.ProgramaRepository;
import com.app.idoneos.repository.modulo_inscripciones.DescuentoRepository;
import com.app.idoneos.repository.modulo_inscripciones.InscripcionRepository;
import com.app.idoneos.repository.modulo_inscripciones.PagoRepository;
import com.app.idoneos.repository.modulo_usuarios.AlumnoRepository;
import com.app.idoneos.repository.modulo_usuarios.UsuarioRepository;
import com.app.idoneos.service.modulo_inscripciones.DescuentoService;
import com.app.idoneos.service.modulo_inscripciones.InscripcionService;
import com.app.idoneos.service.modulo_inscripciones.PagoService;
import com.app.idoneos.service.modulo_inscripciones.ProgresoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de Pruebas de Integración para MOD-F-03: Módulo de Inscripciones y Pagos.
 * Casos de Uso cubiertos:
 * - CU-43: Buscar inscripción
 * - CU-44: Inscribir curso (Modal / Formulario)
 * - CU-45: Dar de baja inscripción (Modal)
 * - CU-46: Buscar pago y consultar comprobante
 * - CU-47: Realizar pago (MODO QR / Pasarela)
 * - CU-48: Buscar progreso pedagógico
 * - CU-49: Buscar descuento
 * - CU-50: Registrar descuento (Modal)
 * - CU-51: Modificar descuento (Modal)
 * - CU-52: Dar de baja descuento (Modal)
 */
@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=demo-id",
    "spring.security.oauth2.client.registration.google.client-secret=demo-secret"
})
@AutoConfigureMockMvc
@Transactional
public class Modulo03InscripcionesIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private InscripcionService inscripcionService;
    @Autowired private PagoService pagoService;
    @Autowired private ProgresoService progresoService;
    @Autowired private DescuentoService descuentoService;

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private DescuentoRepository descuentoRepository;

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-43: Buscar inscripciones con rol Administrador")
    void testCU43_BuscarInscripcionesAdmin() throws Exception {
        mockMvc.perform(get("/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inscripciones/cu-43-buscar-inscripcion"))
                .andExpect(model().attributeExists("inscripciones"))
                .andExpect(model().attributeExists("cohortes"));
    }

    @Autowired private com.app.idoneos.repository.modulo_usuarios.RolRepository rolRepository;

    @Test
    @WithMockUser(username = "alumno.qa@idoneos.online", roles = {"Alumno"})
    @DisplayName("CU-44: Registrar inscripción de curso con alumno existente")
    void testCU44_InscribirCurso() {
        Rol rolAlumno = rolRepository.findByNombre("Alumno").orElseGet(() -> rolRepository.save(new Rol("Alumno")));
        Usuario alumnoUsuario = usuarioRepository.findByCorreo("alumno.qa@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Alumno", "QA", "alumno.qa@idoneos.online", "123456", rolAlumno);
            u.setDni("99887766");
            u.setEmailValidado(true);
            u.setBaja(false);
            return usuarioRepository.save(u);
        });

        List<Curso> cursos = cursoRepository.findAll().stream().filter(c -> !c.isBaja()).toList();
        assertFalse(cursos.isEmpty(), "Debe existir al menos un curso");
        Curso curso = cursos.get(0);

        boolean yaInscripto = inscripcionService.estaInscripto(alumnoUsuario, curso);
        if (!yaInscripto) {
            Inscripcion inscripcion = inscripcionService.inscribirAlumno(alumnoUsuario, curso);
            assertNotNull(inscripcion);
            assertNotNull(inscripcion.getCohorte());
            assertFalse(inscripcion.getBaja());
            assertTrue(inscripcionService.estaInscripto(alumnoUsuario, curso));
        }
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-45: Dar de baja inscripción (Modal)")
    void testCU45_DarDeBajaInscripcion() throws Exception {
        List<Inscripcion> inscripciones = inscripcionRepository.findAll();
        if (!inscripciones.isEmpty()) {
            Inscripcion i = inscripciones.get(0);
            mockMvc.perform(post("/inscripciones/" + i.getId() + "/baja")
                            .param("motivo", "Baja solicitada por el usuario"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/inscripciones"));

            Inscripcion dadaDeBaja = inscripcionRepository.findById(i.getId()).orElse(null);
            assertNotNull(dadaDeBaja);
            assertTrue(dadaDeBaja.getBaja(), "La inscripción debe marcarse como baja lógica");
        }
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-46: Buscar pagos e historial")
    void testCU46_BuscarPagos() throws Exception {
        mockMvc.perform(get("/inscripciones/pagos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inscripciones/cu-46-buscar-pago"))
                .andExpect(model().attributeExists("pagos"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-48: Buscar progreso pedagógico con rol Administrador/Docente")
    void testCU48_BuscarProgreso() throws Exception {
        mockMvc.perform(get("/inscripciones/progreso"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inscripciones/cu-48-buscar-progreso"))
                .andExpect(model().attributeExists("inscripciones"))
                .andExpect(model().attributeExists("porcentajes"))
                .andExpect(model().attributeExists("atrasos"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-49: Buscar descuentos")
    void testCU49_BuscarDescuentos() throws Exception {
        mockMvc.perform(get("/inscripciones/descuentos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/inscripciones/cu-49-buscar-descuento"))
                .andExpect(model().attributeExists("descuentos"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-50, CU-51, CU-52: Ciclo de vida de descuentos (Alta, Modificación y Baja)")
    void testCU50_CU51_CU52_CicloDescuento() throws Exception {
        // CU-50: Registrar Descuento
        mockMvc.perform(post("/inscripciones/descuentos/guardar")
                        .param("codigo", "TEST2026")
                        .param("porcentaje", "35.0")
                        .param("fechaInicio", "2026-03-01")
                        .param("fechaFin", "2026-12-31")
                        .param("cantidadLimite", "50")
                        .param("cantidadCursosRequeridos", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscripciones/descuentos"));

        List<Descuento> encontrados = descuentoService.buscarDescuentosConFiltros("TEST2026", null);
        assertFalse(encontrados.isEmpty(), "El descuento TEST2026 debe haber sido creado");
        Descuento d = encontrados.get(0);
        assertEquals(35.0f, d.getPorcentaje());
        assertFalse(d.getBaja());

        // CU-51: Modificar Descuento
        mockMvc.perform(post("/inscripciones/descuentos/" + d.getId() + "/editar")
                        .param("codigo", "TEST2026_MOD")
                        .param("porcentaje", "40.0")
                        .param("fechaInicio", "2026-03-01")
                        .param("fechaFin", "2026-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscripciones/descuentos"));

        Descuento modificado = descuentoService.buscarPorId(d.getId()).orElse(null);
        assertNotNull(modificado);
        assertEquals("TEST2026_MOD", modificado.getNombre());
        assertEquals(40.0f, modificado.getPorcentaje());

        // CU-52: Dar de baja Descuento
        mockMvc.perform(post("/inscripciones/descuentos/" + d.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscripciones/descuentos"));

        Descuento dadoDeBaja = descuentoService.buscarPorId(d.getId()).orElse(null);
        assertNotNull(dadoDeBaja);
        assertTrue(dadoDeBaja.getBaja(), "El descuento debe estar marcado como baja lógica");
    }
}
