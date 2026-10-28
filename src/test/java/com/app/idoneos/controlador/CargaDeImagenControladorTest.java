package com.app.idoneos.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;

/**
 * Pruebas de la imagen del curso: se guarda en {@code curso{id}/imagen.{ext}} dentro de la carpeta configurada, se
 * publica en {@code /uploads/curso{id}/{imagen}}, se reemplaza al modificar el curso y las carpetas de tema no se
 * publican.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CargaDeImagenControladorTest {

    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R' };
    private static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F' };

    private static Path carpetaDeCursos;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) throws IOException {
        carpetaDeCursos = Files.createTempDirectory("archivos-cursos");
        registro.add("idoneos.directorio-cursos", carpetaDeCursos::toString);
        // Base de datos propia: este contexto no debe compartir ni recrear la de las demás pruebas
        registro.add("spring.datasource.url", () -> "jdbc:h2:mem:idoneos_imagenes;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoServicioImpl cursoServicio;

    private MockMultipartFile imagen(byte[] contenido, String nombre) {
        return new MockMultipartFile("imagenArchivo", nombre, "image/png", contenido);
    }

    private MockHttpServletRequestBuilder formularioDeAlta(String nombre, MockMultipartFile imagen) {
        return multipart("/curso/registrar").file(imagen).param("nombre", nombre).param("precio", "1000")
                .param("cargaHoraria", "20").param("duracionMaxima", "12").param("duracionMaximaUnidad", "semanas")
                .param("categoriaId", "1").param("nivelId", "1").param("idsModalidades", "2")
                .param("docenteTitularId", "1");
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al registrar un curso con imagen, se guarda en la carpeta del curso y se publica en /uploads")
    void registraCursoConImagen() throws Exception {
        mockMvc.perform(formularioDeAlta("Curso con imagen", imagen(PNG, "foto.png")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").exists());

        Curso curso = cursoServicio.buscarPorNombre("Curso con imagen").orElseThrow();
        assertEquals("imagen.png", curso.getImagen());
        assertTrue(Files.exists(carpetaDeCursos.resolve("curso" + curso.getIdCurso()).resolve("imagen.png")));
        assertTrue(curso.getUrlImagen().startsWith("/uploads/curso" + curso.getIdCurso() + "/imagen.png?v="));

        mockMvc.perform(get("/uploads/curso" + curso.getIdCurso() + "/imagen.png")).andExpect(status().isOk())
                .andExpect(content().bytes(PNG));
    }

    @Test
    @DisplayName("La imagen de un curso es pública: un visitante sin sesión la puede ver")
    void laImagenEsPublica() throws Exception {
        Curso curso = cursoServicio.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow();

        mockMvc.perform(get("/uploads/curso" + curso.getIdCurso() + "/" + curso.getImagen()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Las imágenes de ejemplo de la semilla se llevan a la carpeta de cada curso al arrancar")
    void migraLasImagenesDeLaSemilla() throws Exception {
        Curso curso = cursoServicio.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow();

        assertEquals("imagen.jpg", curso.getImagen());
        assertTrue(Files.exists(carpetaDeCursos.resolve("curso" + curso.getIdCurso()).resolve("imagen.jpg")));
    }

    @Test
    @DisplayName("Las imágenes decorativas incluidas en la aplicación se siguen publicando en /img/cursos")
    void publicaLasImagenesDeEjemplo() throws Exception {
        mockMvc.perform(get("/img/cursos/mercado-capitales-argentino.jpg")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Los archivos de las carpetas de tema no se publican por la URL de las imágenes")
    void lasCarpetasDeTemaNoSePublican() throws Exception {
        Curso curso = cursoServicio.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow();
        Path carpetaDeTema = carpetaDeCursos.resolve("curso" + curso.getIdCurso()).resolve("tema1");
        Files.createDirectories(carpetaDeTema);
        Files.write(carpetaDeTema.resolve("material1.mp4"), new byte[] { 0, 0, 0, 0x18, 'f', 't', 'y', 'p' });

        mockMvc.perform(get("/uploads/curso" + curso.getIdCurso() + "/tema1/material1.mp4"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Una imagen inválida no se guarda y el curso no se registra")
    void rechazaImagenInvalida() throws Exception {
        long antes = Files.list(carpetaDeCursos).count();

        mockMvc.perform(formularioDeAlta("Curso con imagen falsa", imagen("no es una imagen".getBytes(), "falsa.png")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("JPG, PNG o WebP")));

        assertFalse(cursoServicio.buscarPorNombre("Curso con imagen falsa").isPresent());
        assertEquals(antes, Files.list(carpetaDeCursos).count());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al modificar el curso con una imagen nueva, se reemplaza la anterior y no queda ninguna huérfana")
    void reemplazaLaImagenAnterior() throws Exception {
        mockMvc.perform(formularioDeAlta("Curso a reemplazar", imagen(PNG, "uno.png")));
        Curso curso = cursoServicio.buscarPorNombre("Curso a reemplazar").orElseThrow();
        Path carpeta = carpetaDeCursos.resolve("curso" + curso.getIdCurso());
        assertTrue(Files.exists(carpeta.resolve("imagen.png")));

        mockMvc.perform(multipart("/curso/modificar/" + curso.getIdCurso()).file(imagen(JPG, "dos.jpg"))
                .param("nombre", "Curso a reemplazar").param("precio", "2000").param("cargaHoraria", "20").param("duracionMaxima", "12").param("duracionMaximaUnidad", "semanas").param("categoriaId", "1")
                .param("nivelId", "1").param("idsModalidades", "2").param("docenteTitularId", "1"))
                .andExpect(status().isOk());

        Curso modificado = cursoServicio.buscarPorNombre("Curso a reemplazar").orElseThrow();
        assertNotNull(modificado.getImagen());
        assertEquals("imagen.jpg", modificado.getImagen());
        assertFalse(Files.exists(carpeta.resolve("imagen.png")), "no debe quedar la imagen anterior");
        assertTrue(Files.exists(carpeta.resolve("imagen.jpg")));
        assertEquals(1, carpeta.toFile().list().length);
    }
}
