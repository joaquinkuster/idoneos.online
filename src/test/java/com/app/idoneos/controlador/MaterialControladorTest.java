package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.TemaRecurso;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.TemaRecursoRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;

/**
 * Pruebas del ABM de materiales (CU-28 a CU-31): pantalla, permisos por rol y formularios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private TemaRecursoRepositorio materialRepositorio;

    private int idCurso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow()
                .getIdCurso();
    }

    private int idCursoPrincipal() {
        return idCurso("Mercado de Capitales Argentino");
    }

    private Tema temaDeRentaFija() {
        return temaRepositorio.findAll().stream().filter(u -> u.getTitulo().equals("Renta fija y bonos")
                && u.getCurso().getNombre().equals("Mercado de Capitales Argentino")).findFirst().orElseThrow();
    }

    private MockHttpServletRequestBuilder subir(String titulo, String nombreArchivo, byte[] contenido) {
        return multipart("/material/subir").file(new MockMultipartFile("archivo", nombreArchivo, "video/mp4", contenido))
                .param("temaId", String.valueOf(temaDeRentaFija().getIdTema()))
                .param("tipo", "GRABACION").param("terminos", "true").param("titulo", titulo);
    }

    // ------------------------------------------------------------------ CU-28 Buscar material

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-28: el docente ve el material de su curso, incluido el oculto, con el selector de recursos")
    void docenteBuscaMaterial() throws Exception {
        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCursoPrincipal())))
                .andExpect(status().isOk()).andExpect(view().name("pages/material/buscar"))
                .andExpect(content().string(Matchers.containsString("Listado de materiales")))
                .andExpect(content().string(Matchers.containsString("Grabación: Estructura del mercado argentino")))
                .andExpect(content().string(Matchers.containsString("Borrador: ejercicios de duration")))
                .andExpect(content().string(Matchers.containsString("Oculto")))
                .andExpect(content().string(Matchers.containsString("Subir material")))
                // sin botón "Ver": el material se abre desde su tema
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalVerMaterial"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-btn-ver"))))
                // "Subir material" abre el formulario directamente, con el tipo como un campo más (sin selector)
                .andExpect(content().string(Matchers.containsString("data-bs-target=\"#modalNuevoMaterial\"")))
                .andExpect(content().string(Matchers.containsString("name=\"tipo\"")))
                .andExpect(content().string(Matchers.containsString("Tipo de material")))
                // los documentos son solo PDF
                .andExpect(content().string(Matchers.containsString("data-extensiones=\"pdf\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Recomendamos PDF"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Agregar un recurso o actividad"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalSelectorRecurso"))))
                // la sidebar de gestión académica con "Materiales" activo y funcionando
                .andExpect(content().string(Matchers.containsString("Contenido de los temas")))
                .andExpect(content().string(Matchers.containsString("/material/buscar?cursoId=")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-28: el título que llega por la URL no recorta el listado (se filtra en la pantalla)")
    void filtraLaBusqueda() throws Exception {
        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCursoPrincipal()))
                .param("titulo", "borrador"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Borrador: ejercicios de duration")))
                .andExpect(content().string(Matchers.containsString("Ley 26.831 y modificatorias CNV")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-29: con ?nuevo={tipo} la página abre sola el formulario de ese tipo en el tema elegido")
    void abreElFormularioDelTipoElegido() throws Exception {
        String grabacion = "GRABACION";
        int tema = temaDeRentaFija().getIdTema();

        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCursoPrincipal()))
                .param("temaId", String.valueOf(tema)).param("nuevo", grabacion))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-abrir-tipo=\"" + grabacion + "\"")))
                .andExpect(content().string(Matchers.containsString("data-tema=\"" + tema + "\"")));

        // un tipo que no se sube a mano (Resumen) se ignora
        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCursoPrincipal()))
                .param("nuevo", "RESUMEN"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-tipo"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-28: sin curso se avisa y se vuelve a Mis cursos")
    void sinCurso() throws Exception {
        mockMvc.perform(get("/material/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"))
                .andExpect(flash().attribute("error", Matchers.containsString("seleccionar un curso")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-28: un docente no accede al material de un curso en el que no participa")
    void docenteAjenoAlCurso() throws Exception {
        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCurso("Macroeconomía de Coyuntura"))))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"))
                .andExpect(flash().attribute("error", Matchers.containsString("No participás")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al ABM de materiales")
    void alumnoSinAcceso() throws Exception {
        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCursoPrincipal())))
                .andExpect(status().isForbidden());
        mockMvc.perform(subir("Clase", "clase.mp4", MP4)).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-28: el administrador busca el material de cualquier curso, pero no sube ni modifica")
    void administradorSoloBuscaYDaDeBaja() throws Exception {
        mockMvc.perform(get("/material/buscar").param("cursoId", String.valueOf(idCursoPrincipal())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Borrador: ejercicios de duration")))
                .andExpect(content().string(Matchers.containsString("Dar de baja")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Subir material"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalNuevoMaterial"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarMaterial"))));
        mockMvc.perform(subir("Clase", "clase.mp4", MP4)).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ CU-29, CU-30 y CU-31

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-29: el docente sube una grabación y el formulario responde con el mensaje de éxito")
    void subeUnaGrabacion() throws Exception {
        long antes = materialRepositorio.count();

        mockMvc.perform(subir("Clase 3: Valuación de bonos", "clase.mp4", MP4)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("subido con éxito")));

        org.junit.jupiter.api.Assertions.assertEquals(antes + 1, materialRepositorio.count());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-29: un archivo que no corresponde al tipo se rechaza con el motivo y no se registra")
    void rechazaUnArchivoInvalido() throws Exception {
        long antes = materialRepositorio.count();

        mockMvc.perform(subir("Clase falsa", "clase.mp4", PDF)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("MP4 o WEBM")));
        mockMvc.perform(subir(" ", "clase.mp4", MP4)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("título")));

        org.junit.jupiter.api.Assertions.assertEquals(antes, materialRepositorio.count());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-30: el docente modifica un material")
    void modificaUnMaterial() throws Exception {
        TemaRecurso material = materialRepositorio.findAll().stream()
                .filter(m -> m.getTitulo().equals("Guía teórica de renta fija")).findFirst().orElseThrow();

        mockMvc.perform(post("/material/modificar/" + material.getIdTemaRecurso()).param("titulo", "Guía actualizada")
                .param("autor", "Autor nuevo").param("isbn", "978-0-00-000000-0").param("oculto", "true")).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("modificado")));

        org.junit.jupiter.api.Assertions.assertEquals("Guía actualizada", material.getTitulo());
        org.junit.jupiter.api.Assertions.assertTrue(material.getOculto());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-31: el docente da de baja un material y una baja masiva es todo o nada")
    void daDeBaja() throws Exception {
        TemaRecurso uno = materialRepositorio.findAll().stream()
                .filter(m -> m.getTitulo().equals("Guía teórica de renta fija")).findFirst().orElseThrow();
        TemaRecurso dos = materialRepositorio.findAll().stream()
                .filter(m -> m.getTitulo().equals("Borrador: ejercicios de duration")).findFirst().orElseThrow();

        mockMvc.perform(post("/material/darDeBaja/" + uno.getIdTemaRecurso())).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("dado de baja")));
        org.junit.jupiter.api.Assertions.assertTrue(uno.esInactivo());

        // uno ya está de baja: la baja masiva falla y no toca al otro
        mockMvc.perform(post("/material/darDeBajaMasiva").param("ids", String.valueOf(uno.getIdTemaRecurso()))
                .param("ids", String.valueOf(dos.getIdTemaRecurso()))).andExpect(status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertFalse(dos.esInactivo());

        mockMvc.perform(post("/material/darDeBajaMasiva").param("ids", String.valueOf(dos.getIdTemaRecurso())))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertTrue(dos.esInactivo());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-31: el administrador puede dar de baja un material de cualquier curso")
    void administradorDaDeBaja() throws Exception {
        TemaRecurso material = materialRepositorio.findAll().stream()
                .filter(m -> m.getTitulo().equals("Guía teórica de renta fija")).findFirst().orElseThrow();

        mockMvc.perform(post("/material/darDeBaja/" + material.getIdTemaRecurso())).andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertTrue(material.esInactivo());
    }
}
