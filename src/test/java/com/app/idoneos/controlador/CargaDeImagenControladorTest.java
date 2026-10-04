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
 * Pruebas de la carga de la imagen de portada desde el formulario de cursos: se guarda en la carpeta
 * configurada, se publica en {@code /img/cursos/**} y se reemplaza al modificar el curso.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CargaDeImagenControladorTest {

    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R' };

    private static Path carpetaDeImagenes;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) throws IOException {
        carpetaDeImagenes = Files.createTempDirectory("imagenes-cursos");
        registro.add("idoneos.directorio-imagenes", carpetaDeImagenes::toString);
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
                .param("categoriaId", "1").param("nivelId", "1").param("idsModalidades", "2")
                .param("docenteTitularId", "1");
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al registrar un curso con imagen, se guarda en la carpeta del servidor y se publica en /img/cursos")
    void registraCursoConImagen() throws Exception {
        mockMvc.perform(formularioDeAlta("Curso con imagen", imagen(PNG, "portada.png")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").exists());

        Curso curso = cursoServicio.buscarPorNombre("Curso con imagen").orElseThrow();
        assertNotNull(curso.getImagen());
        assertTrue(curso.getImagen().endsWith(".png"));
        assertTrue(Files.exists(carpetaDeImagenes.resolve(curso.getImagen())));

        mockMvc.perform(get("/img/cursos/" + curso.getImagen())).andExpect(status().isOk())
                .andExpect(content().bytes(PNG));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Las imágenes de ejemplo incluidas en la aplicación se siguen publicando en /img/cursos")
    void publicaLasImagenesDeEjemplo() throws Exception {
        mockMvc.perform(get("/img/cursos/mercado-capitales-argentino.jpg")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Una imagen inválida no se guarda y el curso no se registra")
    void rechazaImagenInvalida() throws Exception {
        long antes = Files.list(carpetaDeImagenes).count();

        mockMvc.perform(formularioDeAlta("Curso con imagen falsa", imagen("no es una imagen".getBytes(), "falsa.png")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("JPG, PNG o WebP")));

        assertFalse(cursoServicio.buscarPorNombre("Curso con imagen falsa").isPresent());
        assertEquals(antes, Files.list(carpetaDeImagenes).count());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al modificar el curso con una imagen nueva, se elimina la anterior")
    void reemplazaLaImagenAnterior() throws Exception {
        mockMvc.perform(formularioDeAlta("Curso a reemplazar", imagen(PNG, "uno.png")));
        Curso curso = cursoServicio.buscarPorNombre("Curso a reemplazar").orElseThrow();
        String anterior = curso.getImagen();

        mockMvc.perform(multipart("/curso/modificar/" + curso.getIdCurso()).file(imagen(PNG, "dos.png"))
                .param("nombre", "Curso a reemplazar").param("precio", "2000").param("categoriaId", "1")
                .param("nivelId", "1").param("idsModalidades", "2").param("docenteTitularId", "1"))
                .andExpect(status().isOk());

        String nueva = cursoServicio.buscarPorNombre("Curso a reemplazar").orElseThrow().getImagen();
        assertFalse(anterior.equals(nueva));
        assertFalse(Files.exists(carpetaDeImagenes.resolve(anterior)));
        assertTrue(Files.exists(carpetaDeImagenes.resolve(nueva)));
    }
}
