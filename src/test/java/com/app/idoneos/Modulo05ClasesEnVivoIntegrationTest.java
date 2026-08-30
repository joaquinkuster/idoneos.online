package com.app.idoneos;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_clases_vivo.ClaseEnVivoRepository;
import com.app.idoneos.repository.modulo_clases_vivo.EstadoClaseEnVivoRepository;
import com.app.idoneos.repository.modulo_cursos.CohorteRepository;
import com.app.idoneos.repository.modulo_cursos.CursoRepository;
import com.app.idoneos.repository.modulo_usuarios.DocenteRepository;
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

/**
 * Suite de Pruebas de Integración exhaustiva para MOD-F-05: Módulo de Clases en Vivo
 * Casos de Uso: CU-65 a CU-72
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class Modulo05ClasesEnVivoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ClaseEnVivoRepository claseEnVivoRepository;
    @Autowired private EstadoClaseEnVivoRepository estadoRepo;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private DocenteRepository docenteRepository;

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-65: Buscar clases en vivo con filtros por curso, cohorte, texto y estado")
    void testCU65_BuscarClasesEnVivo() throws Exception {
        // 1. Búsqueda base sin parámetros
        mockMvc.perform(get("/clases-vivo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-65-buscar-clase-en-vivo"))
                .andExpect(model().attributeExists("clases"))
                .andExpect(model().attributeExists("cohortes"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));

        // 2. Búsqueda con filtro de estado 'Programada'
        mockMvc.perform(get("/clases-vivo")
                .param("estado", "Programada")
                .param("q", "Taller")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-65-buscar-clase-en-vivo"))
                .andExpect(model().attributeExists("clases"));
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-66: Programar clase en vivo (Formulario y Guardado)")
    void testCU66_ProgramarClaseEnVivo() throws Exception {
        List<Cohorte> cohortes = cohorteRepository.findAll();
        assertFalse(cohortes.isEmpty(), "Debe existir al menos una cohorte");
        Cohorte cohorte = cohortes.get(0);

        // Vista formulario
        mockMvc.perform(get("/clases-vivo/nueva").param("cohorteId", String.valueOf(cohorte.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-66-programar-clase-en-vivo"));

        // Post guardar clase programada
        String fechaFutura = LocalDateTime.now().plusDays(10).withHour(20).withMinute(0).toString().substring(0, 16);
        mockMvc.perform(post("/clases-vivo/guardar")
                .param("cohorteId", String.valueOf(cohorte.getId()))
                .param("titulo", "Clase de Consultas Especial")
                .param("fechaHora", fechaFutura)
                .param("duracionEstimada", "75"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-vivo"))
                .andExpect(flash().attributeExists("mensaje"));

        List<ClaseEnVivo> guardadas = claseEnVivoRepository.findAll().stream()
                .filter(c -> "Clase de Consultas Especial".equals(c.getTitulo()))
                .toList();
        assertFalse(guardadas.isEmpty(), "La clase debe haberse persistido en la BD");
        assertEquals(75, guardadas.get(0).getDuracionEstimada());
        assertEquals("Programada", guardadas.get(0).getEstadoClaseEnVivo().getNombre());
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-67: Modificar clase en vivo existente")
    void testCU67_ModificarClaseEnVivo() throws Exception {
        List<ClaseEnVivo> clases = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        assertFalse(clases.isEmpty(), "Debe existir al menos una clase");
        ClaseEnVivo clase = clases.get(0);

        // Vista formulario editar
        mockMvc.perform(get("/clases-vivo/" + clase.getIdClaseEnVivo() + "/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-67-modificar-clase-en-vivo"))
                .andExpect(model().attributeExists("clase"));

        // Post actualización
        String nuevaFecha = LocalDateTime.now().plusDays(12).withHour(18).withMinute(30).toString().substring(0, 16);
        mockMvc.perform(post("/clases-vivo/" + clase.getIdClaseEnVivo() + "/editar")
                .param("titulo", "Título Modificado con Éxito")
                .param("fechaHora", nuevaFecha)
                .param("duracionEstimada", "90"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-vivo"))
                .andExpect(flash().attributeExists("mensaje"));

        ClaseEnVivo modificada = claseEnVivoRepository.findById(clase.getIdClaseEnVivo()).orElseThrow();
        assertEquals("Título Modificado con Éxito", modificada.getTitulo());
        assertEquals(90, modificada.getDuracionEstimada());
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-68: Cancelar clase en vivo (Formulario y Transición de Estado)")
    void testCU68_CancelarClaseEnVivo() throws Exception {
        List<ClaseEnVivo> clases = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        assertFalse(clases.isEmpty(), "Debe existir al menos una clase");
        ClaseEnVivo clase = clases.get(0);

        // Vista modal/página de confirmación
        mockMvc.perform(get("/clases-vivo/" + clase.getIdClaseEnVivo() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-68-cancelar-clase-en-vivo"))
                .andExpect(model().attributeExists("clase"));

        // Post cancelación
        mockMvc.perform(post("/clases-vivo/" + clase.getIdClaseEnVivo() + "/cancelar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-vivo"))
                .andExpect(flash().attributeExists("mensaje"));

        ClaseEnVivo cancelada = claseEnVivoRepository.findById(clase.getIdClaseEnVivo()).orElseThrow();
        assertEquals("Cancelada", cancelada.getEstadoClaseEnVivo().getNombre());
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-69: Dar de baja lógica clase en vivo")
    void testCU69_DarDeBajaClaseEnVivo() throws Exception {
        List<ClaseEnVivo> clases = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        assertFalse(clases.isEmpty(), "Debe existir al menos una clase");
        ClaseEnVivo clase = clases.get(0);

        // Vista modal/página de confirmación
        mockMvc.perform(get("/clases-vivo/" + clase.getIdClaseEnVivo() + "/baja"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-69-dar-de-baja-clase-en-vivo"))
                .andExpect(model().attributeExists("clase"));

        // Post baja
        mockMvc.perform(post("/clases-vivo/" + clase.getIdClaseEnVivo() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-vivo"))
                .andExpect(flash().attributeExists("mensaje"));

        ClaseEnVivo eliminada = claseEnVivoRepository.findById(clase.getIdClaseEnVivo()).orElseThrow();
        assertTrue(eliminada.getBaja(), "La clase debe tener baja lógica en true");
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-70: Iniciar transmisión de clase en vivo")
    void testCU70_IniciarClaseEnVivo() throws Exception {
        List<ClaseEnVivo> clases = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        assertFalse(clases.isEmpty(), "Debe existir al menos una clase");
        ClaseEnVivo clase = clases.get(0);

        // Vista sala docente / inicio
        mockMvc.perform(get("/clases-vivo/" + clase.getIdClaseEnVivo() + "/iniciar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-70-iniciar-clase-en-vivo"))
                .andExpect(model().attributeExists("clase"));

        // Post iniciar transmisión
        mockMvc.perform(post("/clases-vivo/" + clase.getIdClaseEnVivo() + "/iniciar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-vivo/" + clase.getIdClaseEnVivo() + "/sala"))
                .andExpect(flash().attributeExists("mensaje"));

        ClaseEnVivo enVivo = claseEnVivoRepository.findById(clase.getIdClaseEnVivo()).orElseThrow();
        assertEquals("En vivo", enVivo.getEstadoClaseEnVivo().getNombre());
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-71: Finalizar clase en vivo y publicar grabación")
    void testCU71_FinalizarClaseEnVivo() throws Exception {
        List<ClaseEnVivo> clases = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        assertFalse(clases.isEmpty(), "Debe existir al menos una clase");
        ClaseEnVivo clase = clases.get(0);

        // Vista confirmación finalizar
        mockMvc.perform(get("/clases-vivo/" + clase.getIdClaseEnVivo() + "/finalizar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-71-finalizar-clase-en-vivo"))
                .andExpect(model().attributeExists("clase"));

        // Post finalizar
        mockMvc.perform(post("/clases-vivo/" + clase.getIdClaseEnVivo() + "/finalizar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-vivo"))
                .andExpect(flash().attributeExists("mensaje"));

        ClaseEnVivo finalizada = claseEnVivoRepository.findById(clase.getIdClaseEnVivo()).orElseThrow();
        assertEquals("Finalizada", finalizada.getEstadoClaseEnVivo().getNombre());
    }

    @Test
    @WithMockUser(username = "alumno@idoneos.online", roles = {"Alumno"})
    @DisplayName("CU-72: Ingresar a sala de clase en vivo como alumno")
    void testCU72_IngresarASalaClaseEnVivo() throws Exception {
        List<ClaseEnVivo> clases = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        assertFalse(clases.isEmpty(), "Debe existir al menos una clase");
        ClaseEnVivo clase = clases.get(0);

        mockMvc.perform(get("/clases-vivo/" + clase.getIdClaseEnVivo() + "/sala"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ia_vivo/cu-72-ingresar-a-clase-en-vivo"))
                .andExpect(model().attributeExists("clase"));
    }
}
