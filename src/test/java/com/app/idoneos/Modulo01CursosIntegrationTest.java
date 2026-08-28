package com.app.idoneos;

import com.app.idoneos.model.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=demo-id",
    "spring.security.oauth2.client.registration.google.client-secret=demo-secret"
})
@AutoConfigureMockMvc
public class Modulo01CursosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CohorteService cohorteService;

    @Test
    @DisplayName("CU-06: Explorar catálogo de cursos público (sin autenticación)")
    void testCU06_ExplorarCatalogoPublico() throws Exception {
        mockMvc.perform(get("/cursos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-06-explorar-catalogo-de-cursos"))
                .andExpect(model().attributeExists("cursos"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    @WithMockUser(username = "fausto.spotorno@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-01: Buscar cursos con rol Docente (acceso permitido)")
    void testCU01_BuscarCursosDocente() throws Exception {
        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-01-buscar-curso"))
                .andExpect(model().attributeExists("cursos"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-01: Buscar cursos con rol Administrador (acceso permitido)")
    void testCU01_BuscarCursosAdmin() throws Exception {
        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-01-buscar-curso"))
                .andExpect(model().attributeExists("cursos"));
    }

    @Test
    @WithMockUser(username = "alumno@correo.com", roles = {"Alumno"})
    @DisplayName("CU-02: Ver mis cursos con rol Alumno")
    void testCU02_VerMisCursosAlumno() throws Exception {
        mockMvc.perform(get("/cursos/mis-cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-02-ver-mis-cursos"))
                .andExpect(model().attributeExists("misCursos"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-03: Formulario para registrar nuevo curso (Admin)")
    void testCU03_FormularioNuevoCursoAdmin() throws Exception {
        mockMvc.perform(get("/cursos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-03-registrar-curso"))
                .andExpect(model().attributeExists("categorias"))
                .andExpect(model().attributeExists("docentes"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-07: Buscar categorías (Admin)")
    void testCU07_BuscarCategoriasAdmin() throws Exception {
        mockMvc.perform(get("/cursos/categorias"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-07-buscar-categoria"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-08: Formulario registrar nueva categoría (Admin)")
    void testCU08_FormularioNuevaCategoriaAdmin() throws Exception {
        mockMvc.perform(get("/cursos/categorias/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-08-registrar-categoria"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-08: Guardar nueva categoría válida (Admin)")
    void testCU08_GuardarCategoriaValida() throws Exception {
        String nombreCategoriaTest = "Categoría Test Automatizado " + System.currentTimeMillis();
        mockMvc.perform(post("/cursos/categorias/guardar")
                .param("nombre", nombreCategoriaTest)
                .param("descripcion", "Descripción de prueba para test automatizado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos/categorias"))
                .andExpect(flash().attributeExists("mensaje"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-11: Buscar cohortes de cursos (Admin)")
    void testCU11_BuscarCohortesAdmin() throws Exception {
        mockMvc.perform(get("/cursos/cohortes"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-11-buscar-cohorte"))
                .andExpect(model().attributeExists("cursos"));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-12: Formulario registrar nueva cohorte (Admin)")
    void testCU12_FormularioNuevaCohorteAdmin() throws Exception {
        mockMvc.perform(get("/cursos/cohortes/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-12-registrar-cohorte"))
                .andExpect(model().attributeExists("cursos"));
    }
}
